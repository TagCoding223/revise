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

        // Executes database operations in the background
        this.executorService = Executors.newSingleThreadExecutor();
        // Pushes the results back to the Main UI Thread so your RecyclerView can update
        this.mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    // A custom callback interface so the UI knows when the Repository finishes its work
    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    // ==========================================
    // 1. FETCH TOPICS (The Offline-First Logic)
    // ==========================================
    public void getTopics(RepositoryCallback<List<Topic>> callback) {
        executorService.execute(() -> {
            // STEP 1: Instantly load whatever is saved offline in the database
            List<Topic> localTopics = topicDao.getAllTopics();
            if (localTopics != null && !localTopics.isEmpty()) {
                mainThreadHandler.post(() -> callback.onSuccess(localTopics));
            }

            // STEP 2: Silently reach out to the backend for fresh data
            apiService.getAllTopics().enqueue(new Callback<List<Topic>>() {
                @Override
                public void onResponse(Call<List<Topic>> call, Response<List<Topic>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Topic> remoteTopics = response.body();

                        // Save the fresh data to Room so it's ready for next time
                        executorService.execute(() -> {
                            topicDao.clearAll(); // Clear old deleted records
                            topicDao.insertTopics(remoteTopics);

                            // Send the updated data back to the UI
                            mainThreadHandler.post(() -> callback.onSuccess(remoteTopics));
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<Topic>> call, Throwable t) {
                    // If the network fails, and we had no offline data, tell the UI there's an error.
                    // If we *did* have offline data, the user already saw it in Step 1!
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
        apiService.createTopic(request).enqueue(new Callback<Topic>() {
            @Override
            public void onResponse(Call<Topic> call, Response<Topic> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Topic newTopic = response.body();

                    // Save ONLY this new topic to the local DB, avoiding a full re-fetch
                    executorService.execute(() -> {
                        topicDao.insertTopic(newTopic);
                        mainThreadHandler.post(() -> callback.onSuccess(newTopic));
                    });
                } else {
                    callback.onError("Failed to create topic on server.");
                }
            }

            @Override
            public void onFailure(Call<Topic> call, Throwable t) {
                callback.onError("Network unreachable.");
            }
        });
    }

    // ==========================================
    // 3. UPDATE TOPIC
    // ==========================================
    public void updateTopic(String topicId, TopicRequest request, RepositoryCallback<Topic> callback) {
        apiService.updateTopic(topicId, request).enqueue(new Callback<Topic>() {
            @Override
            public void onResponse(Call<Topic> call, Response<Topic> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Topic updatedTopic = response.body();

                    // Overwrite the existing local record
                    executorService.execute(() -> {
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
    }

    // ==========================================
    // 4. DELETE TOPIC
    // ==========================================
    public void deleteTopic(String topicId, RepositoryCallback<Void> callback) {
        apiService.deleteTopic(topicId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Remove it locally without fetching the whole list again
                    executorService.execute(() -> {
                        topicDao.deleteTopic(topicId);
                        mainThreadHandler.post(() -> callback.onSuccess(null));
                    });
                } else {
                    callback.onError("Failed to delete topic.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network unreachable.");
            }
        });
    }

    // ==========================================
    // 5. REVISE TOPIC
    // ==========================================
    public void reviseTopic(String topicId, RepositoryCallback<Topic> callback) {
        apiService.reviseTopic(topicId).enqueue(new Callback<Topic>() {
            @Override
            public void onResponse(Call<Topic> call, Response<Topic> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Topic revisedTopic = response.body();
                    executorService.execute(() -> {
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
    }
}