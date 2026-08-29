package com.revise.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.revise.entity.RevisionTopic;

public interface TopicRepository extends JpaRepository<RevisionTopic, String>{

    // Automatically generates SQL: SELECT * FROM revision_topics WHERE user_id = ?
    List<RevisionTopic> findAllByUserId(String userId);
    
    // Highly optimized DB query
    // This translates to: SELECT * FROM topics WHERE user_id = ? AND next_revision_date <= ?
    List<RevisionTopic> findAllByUserIdAndNextRevisionDateLessThanEqual(String userId, LocalDateTime date);

    // Pull Sync Query for Android: Fetch EVERYTHING updated recently, including deleted ones!
    // Translates to: SELECT * FROM topics WHERE user_id = ? AND updated_at >= ?
    List<RevisionTopic> findByUserIdAndUpdatedAtGreaterThanEqual(String userId, LocalDateTime since);

    // Web Dashboard: Only fetch active topics
    List<RevisionTopic> findAllByUserIdAndIsDeletedFalse(String userId);
    
    // Revise All Today: Only fetch active topics
    List<RevisionTopic> findAllByUserIdAndNextRevisionDateLessThanEqualAndIsDeletedFalse(String userId, LocalDateTime date);

    @Modifying
    @Query("DELETE FROM RevisionTopic t WHERE t.isDeleted = true AND t.updatedAt <= :thresholdDate")
    int permanentlyDeleteOldTombstones(@Param("thresholdDate") LocalDateTime thresholdDate);
}
