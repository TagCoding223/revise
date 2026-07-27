package com.revise.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revise.dto.request.TopicRequest;
import com.revise.dto.response.ApiResponse;
import com.revise.dto.response.TopicResponse;
import com.revise.entity.RevisionTopic;
import com.revise.entity.User;
import com.revise.exception.ResourceNotFoundException;
import com.revise.repository.TopicRepository;
import com.revise.repository.UserRepository;
import com.revise.service.TopicService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService{
    
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public TopicResponse createTopic(TopicRequest request, String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RevisionTopic topic = new RevisionTopic();
        topic.setUser(user);
        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());
        topic.setLinks(request.getLinks());

        // Initial state for a brand new topic
        topic.setStage(1);
        topic.setLastRevisionDate(null);
        topic.setNextRevisionDate(LocalDateTime.now()); // Due immediately today

        RevisionTopic savedTopic = topicRepository.save(topic);
        return mapToResponse(savedTopic);
    }

    @Override
    public TopicResponse updateTopic(String topicId, TopicRequest request, String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateTopic'");
    }

    @Override
    public TopicResponse getTopicById(String topicId, String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTopicById'");
    }

    @Override
    public List<TopicResponse> getAllTopicForUser(String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllTopicForUser'");
    }

    @Override
    public ApiResponse deleteTopic(String topicId, String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteTopic'");
    }

    @Override
    public TopicResponse markTopicAsRevised(String topicId, String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'markTopicAsRevised'");
    }
    
    /**
     * Maps the database Entity to the DTO needed by the React frontend.
     * It dynamically calculates the category ('today', 'tomorrow', 'other') 
     * based on the exact current date so your dashboard always sorts correctly.
     */
    private TopicResponse mapToResponse(RevisionTopic topic){
        TopicResponse response = new TopicResponse();
        response.setId(topic.getId());
        response.setTitle(topic.getTitle());
        response.setDescription(topic.getDescription());
        response.setLinks(topic.getLinks());
        response.setStage(topic.getStage());
        response.setLastRevisionDate(topic.getLastRevisionDate());
        response.setNextRevisionDate(topic.getNextRevisionDate());

        // Determine Category for the Dashboard UI
        LocalDate today = LocalDate.now();
        LocalDate targetDate = topic.getNextRevisionDate().toLocalDate();

        if(targetDate.isBefore(today) || targetDate.isEqual(today)){
            response.setCategory("today"); // Overdue or due today
        }else if (targetDate.isEqual(today.plusDays(1))){
            response.setCategory("tomorrow");
        }else{
            response.setCategory("other");
        }

        return response;
    }

}
