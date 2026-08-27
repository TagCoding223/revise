package com.revise.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "topics")
public class Topic {

    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private String description;
    private int stage;
    private String category; // "today", "tomorrow", "other"
    private String lastRevisionDate;
    private String nextRevisionDate;
    private List<String> links;

    public Topic() {
    }

    public Topic(String id, String title, String description, int stage, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.stage = stage;
        this.category = category;
    }

    public List<String> getLinks() { return links; }
    public void setLinks(List<String> links) { this.links = links; }
    public void setCategory(String category) { this.category = category; }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getStage() { return stage; }
    public String getCategory() { return category; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    // Sync tracking fields
    private boolean isSynced = true;
    private boolean isDeleted = false;

    // Getters and Setters
    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public String getLastRevisionDate() { return lastRevisionDate; }
    public String getNextRevisionDate() { return nextRevisionDate; }

    public void setLastRevisionDate(String lastRevisionDate) { this.lastRevisionDate = lastRevisionDate; }
    public void setNextRevisionDate(String nextRevisionDate) { this.nextRevisionDate = nextRevisionDate; }
}
