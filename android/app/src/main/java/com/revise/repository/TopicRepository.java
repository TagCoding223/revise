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
import java.util.Locale;
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

        for (Topic t : unsyncedTopics) {
            batchPayload.add(new TopicSyncRequest(
                    t.getId(), t.getTitle(), t.getDescription(), t.getLinks(), t.getStage(),
                    // Use the stored timestamp, fallback to current UTC if null
                    t.getLastRevisionDate() != null ? t.getLastRevisionDate() : getCurrentUtcTime(),
                    t.getNextRevisionDate() != null ? t.getNextRevisionDate() : getCurrentUtcTime(),
                    t.getUpdatedAt() != null ? t.getUpdatedAt() : getCurrentUtcTime(), // LWW Anchor
                    t.isDeleted()
            ));
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
                        for (Topic t : unsyncedTopics) {
                            if (t.isDeleted()) {
                                // Once the server confirms it received the deletion, hard delete it locally to save space
                                topicDao.deleteTopic(t.getId());
                            } else {
                                t.setSynced(true);
                                topicDao.insertTopic(t);
                            }
                        }
                        pullDeltaSync(callback);
                    });
                } else {
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
    // 3. PULL SYNC (Step 2 of Two-Way Sync & Handles Web Deletions)
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
                            for (Topic t : deltaTopics) {
                                // If the web dashboard deleted this topic, hard delete it locally
                                if (t.isDeleted()) {
                                    topicDao.deleteTopic(t.getId());
                                } else {
                                    t.setSynced(true);
                                    topicDao.insertTopic(t);
                                }
                            }
                        }

                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.US);
                        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                        syncPrefs.edit().putString(PREF_LAST_SYNC, sdf.format(new java.util.Date())).apply();

                        List<Topic> finalTopics = topicDao.getAllTopics();
                        mainThreadHandler.post(() -> callback.onSuccess(finalTopics));
                    });
                } else {
                    mainThreadHandler.post(() -> callback.onError("Failed to fetch recent updates."));
                }
            }
            @Override
            public void onFailure(Call<List<Topic>> call, Throwable t) {
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
            localTopic.setUpdatedAt(getCurrentUtcTime());

            topicDao.insertTopic(localTopic);

            // 2. Update UI. The DashboardFragment will automatically trigger fetchTopics(),
            // which will instantly push this new topic via the batch sync!
            mainThreadHandler.post(() -> callback.onSuccess(localTopic));

            // NOTE: The apiService.createTopic(...) call has been completely removed
            // to prevent race conditions with the batch sync.
        });
    }

    // ==========================================
    // 5. UPDATE TOPIC (True Offline-First)
    // ==========================================
    public void updateTopic(Topic existingTopic, TopicRequest request, RepositoryCallback<Topic> callback) {
        executorService.execute(() -> {
            // 1. Optimistic Local Save ONLY
            existingTopic.setTitle(request.getTitle());
            existingTopic.setDescription(request.getDescription());
            existingTopic.setLinks(request.getLinks());
            existingTopic.setSynced(false);
            existingTopic.setUpdatedAt(getCurrentUtcTime());

            topicDao.insertTopic(existingTopic);

            // 2. Trigger UI update. DashboardFragment will call fetchTopics(),
            // which handles the background batch sync safely without race conditions!
            mainThreadHandler.post(() -> callback.onSuccess(existingTopic));

            // NOTE: apiService.updateTopic(...) has been removed!
        });
    }

    // ==========================================
    // 6. DELETE TOPIC (Fully Offline/Optimistic)
    // ==========================================
    public void deleteTopic(String topicId, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            // 1. Soft delete locally by updating the flag
            List<Topic> all = topicDao.getAllTopics();
            for (Topic t : all) {
                if (t.getId().equals(topicId)) {
                    t.setDeleted(true);
                    t.setSynced(false);
                    t.setUpdatedAt(getCurrentUtcTime()); // Stamp the deletion time
                    topicDao.insertTopic(t);
                    break;
                }
            }

            // 2. Instantly update UI and trigger the Master Sync Orchestrator
            mainThreadHandler.post(() -> callback.onSuccess(null));
        });
    }

    // ==========================================
    // 7. REVISE TOPIC (True Offline-First)
    // ==========================================
    public void reviseTopic(Topic existingTopic, RepositoryCallback<Topic> callback) {
        executorService.execute(() -> {
            // 1. Optimistic Local Update
            int newStage = existingTopic.getStage() + 1;
            existingTopic.setStage(newStage);
            int daysToAdd = calculateSpaceRepetitionInterval(newStage);

            // 2. Format dates accurately for Spring Boot
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.US);
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC")); // CRITICAL FIX: Forces UTC!

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            existingTopic.setLastRevisionDate(sdf.format(calendar.getTime()));

            calendar.add(java.util.Calendar.DAY_OF_YEAR, daysToAdd);
            existingTopic.setNextRevisionDate(sdf.format(calendar.getTime()));

            // 3. Dynamically update UI category
            existingTopic.setCategory(daysToAdd == 1 ? "tomorrow" : "other");
            existingTopic.setSynced(false);
            existingTopic.setUpdatedAt(getCurrentUtcTime());

            topicDao.insertTopic(existingTopic);
            mainThreadHandler.post(() -> callback.onSuccess(existingTopic));

            // NOTE: apiService.reviseTopic(...) has been removed!
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

    private String getCurrentUtcTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return sdf.format(new java.util.Date());
    }
}