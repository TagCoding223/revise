package com.revise.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revise.entity.RevisionTopic;

public interface TopicRepository extends JpaRepository<RevisionTopic, String>{

    // Automatically generates SQL: SELECT * FROM revision_topics WHERE user_id = ?
    List<RevisionTopic> findAllByUserId(String userId);
    
}
