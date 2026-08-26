package com.revise.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.revise.model.Topic;

import java.util.List;

@Dao
public interface TopicDao {

    // REPLACE replaces the old record if the IDs match, perfectly handling updates
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTopics(List<Topic> topics);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTopic(Topic topic);

    // Only fetch topics that are not deleted
    @Query("SELECT * FROM topics WHERE isDeleted = 0")
    List<Topic> getAllTopics();

    // Useful for when we implement background batch-syncing later
    @Query("SELECT * FROM topics WHERE isSynced = 0")
    List<Topic> getUnsyncedTopics();

    @Query("DELETE FROM topics WHERE id = :topicId")
    void deleteTopic(String topicId);

    @Query("DELETE FROM topics")
    void clearAll();
}