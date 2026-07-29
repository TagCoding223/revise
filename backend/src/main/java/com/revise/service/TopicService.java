package com.revise.service;

import java.util.List;

import com.revise.dto.request.TopicRequest;
import com.revise.dto.response.ApiResponse;
import com.revise.dto.response.TopicResponse;

public interface TopicService {
    // Core CRUD Operations
    TopicResponse createTopic(TopicRequest request, String userId);

    TopicResponse updateTopic(String topicId, TopicRequest request, String userId);

    TopicResponse getTopicById(String topicId, String userId);

    List<TopicResponse> getAllTopicForUser(String userId);

    ApiResponse deleteTopic(String topicId, String userId);

    public ApiResponse reviseAllToday(String userId);

    // The core spaced repetition trigger
    TopicResponse markTopicAsRevised(String topicId, String userId);

}
