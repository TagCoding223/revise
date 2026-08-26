package com.revise.repository;

import android.content.Context;
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

    public TopicRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.topicDao = db.topicDao();
        this.apiService = RetrofitClient.getClient(context).create(TopicApiService.class);
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    // ==========================================
    // 1. FETCH TOPICS & BACKGROUND SYNC
    // ==========================================
    public void getTopics(RepositoryCallback<List<Topic>> callback) {
        executorService.execute(() -> {
            // 1. Show local data immediately
            List<Topic> localTopics = topicDao.getAllTopics();
            if (localTopics != null && !localTopics.isEmpty()) {
                mainThreadHandler.post(() -> callback.onSuccess(localTopics));
            }

            // 2. Fetch fresh server data
            apiService.getAllTopics().enqueue(new Callback<List<Topic>>() {
                @Override
                public void onResponse(Call<List<Topic>> call, Response<List<Topic>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        executorService.execute(() -> {
                            // A: BACKUP offline changes before wiping!
                            List<Topic> unsynced = topicDao.getUnsyncedTopics();

                            // B: Wipe and insert fresh server data
                            List<Topic> remoteTopics = response.body();
                            for (Topic t : remoteTopics) { t.setSynced(true); }
                            topicDao.clearAll();
                            topicDao.insertTopics(remoteTopics);

                            // C: RESTORE offline changes so they are not lost
                            if (unsynced != null && !unsynced.isEmpty()) {
                                topicDao.insertTopics(unsynced);

                                // D: Pass the UI callback here!
                                pushOfflineData(unsynced, callback);
                            }

                            // E: Update the UI with the final merged list
                            List<Topic> finalTopics = topicDao.getAllTopics();
                            mainThreadHandler.post(() -> callback.onSuccess(finalTopics));
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<Topic>> call, Throwable t) {
                    if (localTopics == null || localTopics.isEmpty()) {
                        mainThreadHandler.post(() -> callback.onError("Network offline."));
                    }
                }
            });
        });
    }

    // ==========================================
    // HELPER: BATCH SYNC TO BACKEND
    // ==========================================
    // The callback parameter
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
                        currentIsoDate, currentIsoDate, currentIsoDate
                ));
            }
        }

        if (batchPayload.isEmpty()) return;

        apiService.pushSyncBatch(batchPayload).enqueue(new Callback<com.revise.dto.response.ApiResponse>() {
            @Override
            public void onResponse(Call<com.revise.dto.response.ApiResponse> call, Response<com.revise.dto.response.ApiResponse> response) {
                if (response.isSuccessful()) {
                    executorService.execute(() -> {
                        for (Topic t : unsyncedTopics) {
                            if (!t.isDeleted()) {
                                t.setSynced(true); // Database updated
                                topicDao.insertTopic(t);
                            }
                        }

                        // Re-fetch the clean list from the database and refresh the UI!
                        List<Topic> updatedTopics = topicDao.getAllTopics();
                        mainThreadHandler.post(() -> callback.onSuccess(updatedTopics));
                    });
                }
            }

            @Override
            public void onFailure(Call<com.revise.dto.response.ApiResponse> call, Throwable t) {}
        });
    }

    // ==========================================
    // 2. CREATE TOPIC
    // ==========================================
    public void createTopic(TopicRequest request, RepositoryCallback<Topic> callback) {
        executorService.execute(() -> {
            // Optimistic Local Save
            Topic localTopic = new Topic();
            localTopic.setId(UUID.randomUUID().toString());
            localTopic.setTitle(request.getTitle());
            localTopic.setDescription(request.getDescription());
            localTopic.setLinks(request.getLinks());
            localTopic.setCategory("today");
            localTopic.setStage(1);
            localTopic.setSynced(false);

            topicDao.insertTopic(localTopic);
            mainThreadHandler.post(() -> callback.onSuccess(localTopic));

            // Background Network Sync
            apiService.createTopic(request).enqueue(new Callback<Topic>() {
                @Override
                public void onResponse(Call<Topic> call, Response<Topic> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        executorService.execute(() -> {
                            topicDao.deleteTopic(localTopic.getId()); // Remove temp local
                            Topic serverTopic = response.body();
                            serverTopic.setSynced(true);
                            topicDao.insertTopic(serverTopic);
                        });
                    }
                }
                @Override
                public void onFailure(Call<Topic> call, Throwable t) {}
            });
        });
    }

    // ==========================================
    // 3. UPDATE TOPIC (Now Fully Optimistic)
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
    // 4. DELETE TOPIC
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
    // 5. REVISE TOPIC
    // ==========================================
    public void reviseTopic(String topicId, RepositoryCallback<Topic> callback) {
        executorService.execute(() -> {
            apiService.reviseTopic(topicId).enqueue(new Callback<Topic>() {
                @Override
                public void onResponse(Call<Topic> call, Response<Topic> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        executorService.execute(() -> {
                            Topic revisedTopic = response.body();
                            revisedTopic.setSynced(true);
                            topicDao.insertTopic(revisedTopic);
                            mainThreadHandler.post(() -> callback.onSuccess(revisedTopic));
                        });
                    } else {
                        callback.onError("Failed to revise topic.");
                    }
                }
                @Override
                public void onFailure(Call<Topic> call, Throwable t) {
                    callback.onError("Network unreachable.");
                }
            });
        });
    }

}