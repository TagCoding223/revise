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

    @Query("SELECT * FROM topics")
    List<Topic> getAllTopics();

    @Query("DELETE FROM topics WHERE id = :topicId")
    void deleteTopic(String topicId);

    @Query("DELETE FROM topics")
    void clearAll();
}