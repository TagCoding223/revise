package com.revise.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
public class TopicServiceImpl implements TopicService {

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
    @Transactional
    public TopicResponse updateTopic(String topicId, TopicRequest request, String userId) {
        RevisionTopic topic = getTopicEntityOwnedByUser(topicId, userId);

        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());
        topic.setLinks(request.getLinks());

        RevisionTopic updatedTopic = topicRepository.save(topic);
        return mapToResponse(updatedTopic);
    }

    @Override
    public TopicResponse getTopicById(String topicId, String userId) {
        RevisionTopic topic = getTopicEntityOwnedByUser(topicId, userId);
        return mapToResponse(topic);
    }

    @Override
    public List<TopicResponse> getAllTopicForUser(String userId) {
        return topicRepository.findAllByUserId(userId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApiResponse deleteTopic(String topicId, String userId) {
        RevisionTopic topic = getTopicEntityOwnedByUser(topicId, userId);
        topicRepository.delete(topic);
        return new ApiResponse(true, "Topic deleted successfully.");
    }

    @Override
    public TopicResponse markTopicAsRevised(String topicId, String userId) {
        RevisionTopic topic = getTopicEntityOwnedByUser(topicId, userId);

        // Update the stage
        int currentStage = topic.getStage();
        topic.setStage(currentStage + 1);

        // Record exactly when they revised it
        topic.setLastRevisionDate(LocalDateTime.now());

        // Calculate the next revision date based on the new stage
        int daysToAdd = calculateSpaceRepetitionInterval(topic.getStage());
        topic.setNextRevisionDate(LocalDateTime.now().plusDays(daysToAdd));

        RevisionTopic updatedTopic = topicRepository.save(topic);
        return mapToResponse(updatedTopic);
    }

    @Override
    @Transactional
    public ApiResponse reviseAllToday(String userId) {
        // 1. Get the exact end of "today" (23:59:59) according to the Server
        LocalDateTime endOfToday = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        // 2. Let the Database do the heavy lifting!
        // Only loads topics that belong to the user AND are due today or earlier.
        List<RevisionTopic> topicsToRevise = topicRepository
                .findAllByUserIdAndNextRevisionDateLessThanEqual(userId, endOfToday);

        if (topicsToRevise.isEmpty()) {
            return new ApiResponse(true, "No topics to revise today.");
        }

        // 3. Apply spaced repetition logic
        for (RevisionTopic topic : topicsToRevise) {
            topic.setStage(topic.getStage() + 1);
            topic.setLastRevisionDate(LocalDateTime.now());
            int daysToAdd = calculateSpaceRepetitionInterval(topic.getStage());
            topic.setNextRevisionDate(LocalDateTime.now().plusDays(daysToAdd));
        }

        // 4. Save back to the DB
        topicRepository.saveAll(topicsToRevise);
        return new ApiResponse(true, "Successfully revised " + topicsToRevise.size() + " topics.");
    }

    // --- Helper Methods ---

    /**
     * Ensures that a topic exists AND belongs to the requesting user.
     * This is a critical security measure to prevent ID-guessing attacks.
     */
    private RevisionTopic getTopicEntityOwnedByUser(String topicId, String userId) {
        RevisionTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        if (!topic.getUser().getId().equals(userId)) {
            // Reusing ResourceNotFound instead of Unauthorized prevents attackers from
            // knowing if an ID exists but belongs to someone else.

            throw new ResourceNotFoundException("Topic not found");
        }
        return topic;
    }

    /**
     * Spaced Repetition Algorithm.
     * Determines how many days to wait before showing the topic again.
     */
    private int calculateSpaceRepetitionInterval(int stage){
        return switch (stage) {
            case 1 -> 1;// Revisit next day
            case 2 -> 3;  // Revisit in 3 days
            case 3 -> 7;  // Revisit in 1 week
            case 4 -> 14; // Revisit in 2 weeks
            case 5 -> 30; // Revisit in 1 month
            default -> 60; // Max out at 2 months for deeply learned concepts
        };
    }

    /**
     * Maps the database Entity to the DTO needed by the React frontend.
     * It dynamically calculates the category ('today', 'tomorrow', 'other')
     * based on the exact current date so your dashboard always sorts correctly.
     */
    private TopicResponse mapToResponse(RevisionTopic topic) {
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

        if (targetDate.isBefore(today) || targetDate.isEqual(today)) {
            response.setCategory("today"); // Overdue or due today
        } else if (targetDate.isEqual(today.plusDays(1))) {
            response.setCategory("tomorrow");
        } else {
            response.setCategory("other");
        }

        return response;
    }

}
