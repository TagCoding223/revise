package com.revise.network;

import com.revise.dto.request.TopicRequest;
import com.revise.model.Topic;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface TopicApiService {

    // Assuming backend returns an object with arrays: { "today": [...], "tomorrow": [...], "upcoming": [...] }
    @GET("api/v1/topics")
    Call<List<Topic>> getAllTopics();

    @POST("api/v1/topics")
    Call<Topic> createTopic(@Body TopicRequest request);

    @PUT("api/v1/topics/{id}")
    Call<Topic> updateTopic(@Path("id") String id, @Body TopicRequest request);

    @DELETE("api/v1/topics/{id}")
    Call<Void> deleteTopic(@Path("id") String id);

    @PATCH("api/v1/topics/{id}/revise")
    Call<Topic> reviseTopic(@Path("id") String id);
}