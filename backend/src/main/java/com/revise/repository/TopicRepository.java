package com.revise.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revise.entity.RevisionTopic;

public interface TopicRepository extends JpaRepository<RevisionTopic, String>{

    // Automatically generates SQL: SELECT * FROM revision_topics WHERE user_id = ?
    List<RevisionTopic> findAllByUserId(String userId);
    
    // Highly optimized DB query
    // This translates to: SELECT * FROM topics WHERE user_id = ? AND next_revision_date <= ?
    List<RevisionTopic> findAllByUserIdAndNextRevisionDateLessThanEqual(String userId, LocalDateTime date);
}
