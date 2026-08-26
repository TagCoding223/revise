package com.revise.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.revise.database.AppDatabase;
import com.revise.database.TopicDao;
import com.revise.dto.request.TopicRequest;
import com.revise.model.Topic;
import com.revise.network.RetrofitClient;
import com.revise.network.TopicApiService;

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
    // 1. FETCH TOPICS
    // ==========================================
    public void getTopics(RepositoryCallback<List<Topic>> callback) {
        executorService.execute(() -> {
            List<Topic> localTopics = topicDao.getAllTopics();
            if (localTopics != null && !localTopics.isEmpty()) {
                mainThreadHandler.post(() -> callback.onSuccess(localTopics));
            }

            apiService.getAllTopics().enqueue(new Callback<List<Topic>>() {
                @Override
                public void onResponse(Call<List<Topic>> call, Response<List<Topic>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        executorService.execute(() -> {
                            List<Topic> remoteTopics = response.body();
                            // Ensure the remote topics are marked as synced
                            for (Topic t : remoteTopics) { t.setSynced(true); }

                            topicDao.clearAll();
                            topicDao.insertTopics(remoteTopics);

                            mainThreadHandler.post(() -> callback.onSuccess(remoteTopics));
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<Topic>> call, Throwable t) {
                    if (localTopics == null || localTopics.isEmpty()) {
                        mainThreadHandler.post(() -> callback.onError("Network offline. No local data found."));
                    }
                }
            });
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
    // 3. UPDATE TOPIC
    // ==========================================
    public void updateTopic(String topicId, TopicRequest request, RepositoryCallback<Topic> callback) {
        executorService.execute(() -> {
            // Background Network Sync
            apiService.updateTopic(topicId, request).enqueue(new Callback<Topic>() {
                @Override
                public void onResponse(Call<Topic> call, Response<Topic> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        executorService.execute(() -> {
                            Topic updatedTopic = response.body();
                            updatedTopic.setSynced(true);
                            topicDao.insertTopic(updatedTopic);
                            mainThreadHandler.post(() -> callback.onSuccess(updatedTopic));
                        });
                    } else {
                        callback.onError("Failed to update topic.");
                    }
                }
                @Override
                public void onFailure(Call<Topic> call, Throwable t) {
                    callback.onError("Network unreachable.");
                }
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