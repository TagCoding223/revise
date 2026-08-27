package com.revise.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import com.revise.database.AppDatabase;
import com.revise.database.TopicDao;
import com.revise.dto.request.TopicRequest;
import com.revise.dto.request.TopicSyncRequest;
import com.revise.model.Topic;
import com.revise.network.RetrofitClient;
import com.revise.network.TopicApiService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TopicRepository {

    private final TopicDao topicDao;
    private final TopicApiService apiService;
    private final ExecutorService executorService;
    private final Handler mainThreadHandler;
    private final SharedPreferences syncPrefs;
    private static final String PREF_LAST_SYNC = "last_successful_sync";
    private static final String DEFAULT_TIMESTAMP = "2000-01-01T00:00:00.000";

    public TopicRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.topicDao = db.topicDao();
        this.apiService = RetrofitClient.getClient(context).create(TopicApiService.class);
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainThreadHandler = new Handler(Looper.getMainLooper());
        this.syncPrefs = context.getSharedPreferences("SyncPrefs", Context.MODE_PRIVATE);
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    // ==========================================
    // 1. MASTER SYNC ORCHESTRATOR
    // ==========================================
    public void getTopics(RepositoryCallback<List<Topic>> callback) {
        executorService.execute(() -> {
            // A. Show local data instantly for zero-latency UX
            List<Topic> localTopics = topicDao.getAllTopics();
            if (localTopics != null && !localTopics.isEmpty()) {
                mainThreadHandler.post(() -> callback.onSuccess(localTopics));
            }

            // B. Check for unsynced offline changes
            List<Topic> unsynced = topicDao.getUnsyncedTopics();

            if (unsynced != null && !unsynced.isEmpty()) {
                // If offline changes exist, PUSH them first, then PULL deltas
                pushOfflineData(unsynced, callback);
            } else {
                // If no offline changes, jump straight to PULLING deltas
                pullDeltaSync(callback);
            }
        });
    }

    // ==========================================
    // 2. PUSH SYNC (Step 1 of Two-Way Sync)
    // ==========================================
    private void pushOfflineData(List<Topic> unsyncedTopics, RepositoryCallback<List<Topic>> callback) {
        List<TopicSyncRequest> batchPayload = new ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.US);
        String currentIsoDate = sdf.format(new java.util.Date());

        for (Topic t : unsyncedTopics) {
            if (t.isDeleted()) {
                deleteTopic(t.getId(), new RepositoryCallback<Void>() {
                    @Override public void onSuccess(Void data) {}
                    @Override public void onError(String message) {}
                });
            } else {
                batchPayload.add(new TopicSyncRequest(
                        t.getId(), t.getTitle(), t.getDescription(), t.getLinks(), t.getStage(),
                        t.getLastRevisionDate() != null ? t.getLastRevisionDate() : currentIsoDate,
                        t.getNextRevisionDate() != null ? t.getNextRevisionDate() : currentIsoDate,
                        currentIsoDate
                ));
            }
        }

        if (batchPayload.isEmpty()) {
            pullDeltaSync(callback);
            return;
        }

        apiService.pushSyncBatch(batchPayload).enqueue(new Callback<com.revise.dto.response.ApiResponse>() {
            @Override
            public void onResponse(Call<com.revise.dto.response.ApiResponse> call, Response<com.revise.dto.response.ApiResponse> response) {
                if (response.isSuccessful()) {
                    executorService.execute(() -> {
                        // Mark as synced locally
                        for (Topic t : unsyncedTopics) {
                            if (!t.isDeleted()) {
                                t.setSynced(true);
                                topicDao.insertTopic(t);
                            }
                        }
                        // Move to Phase 2: Pull Deltas
                        pullDeltaSync(callback);
                    });
                } else {
                    // Push failed, but we should still try to pull updates
                    pullDeltaSync(callback);
                }
            }
            @Override
            public void onFailure(Call<com.revise.dto.response.ApiResponse> call, Throwable t) {
                pullDeltaSync(callback);
            }
        });
    }

    // ==========================================
    // 3. PULL SYNC (Step 2 of Two-Way Sync)
    // ==========================================
    private void pullDeltaSync(RepositoryCallback<List<Topic>> callback) {
        String lastSyncTime = syncPrefs.getString(PREF_LAST_SYNC, DEFAULT_TIMESTAMP);

        apiService.pullSync(lastSyncTime).enqueue(new Callback<List<Topic>>() {
            @Override
            public void onResponse(Call<List<Topic>> call, Response<List<Topic>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executorService.execute(() -> {
                        List<Topic> deltaTopics = response.body();

                        if (!deltaTopics.isEmpty()) {
                            // Room's OnConflictStrategy.REPLACE automatically handles updates vs new inserts!
                            for (Topic t : deltaTopics) { t.setSynced(true); }
                            topicDao.insertTopics(deltaTopics);
                        }

                        // Record the exact moment this sync finished
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.US);
                        syncPrefs.edit().putString(PREF_LAST_SYNC, sdf.format(new java.util.Date())).apply();

                        // Refresh UI with the finalized database state
                        List<Topic> finalTopics = topicDao.getAllTopics();
                        mainThreadHandler.post(() -> callback.onSuccess(finalTopics));
                    });
                } else {
                    mainThreadHandler.post(() -> callback.onError("Failed to fetch recent updates."));
                }
            }

            @Override
            public void onFailure(Call<List<Topic>> call, Throwable t) {
                // Fails silently if offline; the user is already viewing the cached data.
                mainThreadHandler.post(() -> callback.onError("Network offline. Displaying local data."));
            }
        });
    }

    // ==========================================
    // 4. CREATE TOPIC
    // ==========================================
    public void createTopic(TopicRequest request, RepositoryCallback<Topic> callback) {
        executorService.execute(() -> {
            // 1. Optimistic Local Save ONLY
            Topic localTopic = new Topic();
            localTopic.setId(java.util.UUID.randomUUID().toString()); // Android is the master of IDs
            localTopic.setTitle(request.getTitle());
            localTopic.setDescription(request.getDescription());
            localTopic.setLinks(request.getLinks());
            localTopic.setCategory("today");
            localTopic.setStage(1);
            localTopic.setSynced(false);

            topicDao.insertTopic(localTopic);

            // 2. Update UI. The DashboardFragment will automatically trigger fetchTopics(),
            // which will instantly push this new topic via the batch sync!
            mainThreadHandler.post(() -> callback.onSuccess(localTopic));

            // NOTE: The apiService.createTopic(...) call has been completely removed
            // to prevent race conditions with the batch sync.
        });
    }

    // ==========================================
    // 5. UPDATE TOPIC (Now Fully Optimistic)
    // ==========================================
    // Note: We changed 'String topicId' to 'Topic existingTopic'
    public void updateTopic(Topic existingTopic, TopicRequest request, RepositoryCallback<Topic> callback) {
        executorService.execute(() -> {
            // 1. Optimistic Local Save FIRST
            existingTopic.setTitle(request.getTitle());
            existingTopic.setDescription(request.getDescription());
            existingTopic.setLinks(request.getLinks());
            existingTopic.setSynced(false);

            topicDao.insertTopic(existingTopic);
            mainThreadHandler.post(() -> callback.onSuccess(existingTopic));

            // 2. Background Network Sync
            apiService.updateTopic(existingTopic.getId(), request).enqueue(new Callback<Topic>() {
                @Override
                public void onResponse(Call<Topic> call, Response<Topic> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        executorService.execute(() -> {
                            Topic updatedTopic = response.body();
                            updatedTopic.setSynced(true);
                            topicDao.insertTopic(updatedTopic);
                        });
                    }
                }
                @Override
                public void onFailure(Call<Topic> call, Throwable t) {} // Fails silently, stays offline
            });
        });
    }

    // ==========================================
    // 6. DELETE TOPIC
    // ==========================================
    public void deleteTopic(String topicId, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            // Optimistic Local Delete
            topicDao.deleteTopic(topicId);
            mainThreadHandler.post(() -> callback.onSuccess(null));

            // Background Network Sync
            apiService.deleteTopic(topicId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {}
                @Override
                public void onFailure(Call<Void> call, Throwable t) {}
            });
        });
    }

    // ==========================================
    // 7. REVISE TOPIC (Fully Optimistic)
    // ==========================================
    public void reviseTopic(Topic existingTopic, RepositoryCallback<Topic> callback) {
        executorService.execute(() -> {
            // 1. Optimistic Local Update
            int newStage = existingTopic.getStage() + 1;
            existingTopic.setStage(newStage);

            // Apply Spaced Repetition Logic
            int daysToAdd = calculateSpaceRepetitionInterval(newStage);

            // Format dates accurately for Spring Boot
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.US);
            java.util.Calendar calendar = java.util.Calendar.getInstance();

            existingTopic.setLastRevisionDate(sdf.format(calendar.getTime())); // Revised right now

            calendar.add(java.util.Calendar.DAY_OF_YEAR, daysToAdd);
            existingTopic.setNextRevisionDate(sdf.format(calendar.getTime())); // Next due date

            // Dynamically update UI category so it disappears from the "Today" list instantly
            existingTopic.setCategory(daysToAdd == 1 ? "tomorrow" : "other");
            existingTopic.setSynced(false);

            topicDao.insertTopic(existingTopic);
            mainThreadHandler.post(() -> callback.onSuccess(existingTopic));

            // 2. Background Network Sync
            apiService.reviseTopic(existingTopic.getId()).enqueue(new Callback<Topic>() {
                @Override
                public void onResponse(Call<Topic> call, Response<Topic> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        executorService.execute(() -> {
                            Topic serverTopic = response.body();
                            serverTopic.setSynced(true);
                            topicDao.insertTopic(serverTopic);
                        });
                    }
                }
                @Override
                public void onFailure(Call<Topic> call, Throwable t) {
                    // Fails silently. It remains isSynced=false and will ride the batch sync later!
                }
            });
        });
    }

    private int calculateSpaceRepetitionInterval(int stage) {
        switch (stage) {
            case 1: return 1;
            case 2: return 3;
            case 3: return 7;
            case 4: return 16;
            case 5: return 35;
            case 6: return 120;
            case 7: return 180;
            case 8: return 365;
            default: return 730;
        }
    }
}