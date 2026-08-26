package com.revise.dto.request;

import java.util.List;

public class TopicSyncRequest {
    private String id;
    private String title;
    private String description;
    private List<String> links;
    private int stage;
    private String lastRevisionDate;
    private String nextRevisionDate;
    private String updatedAt;

    public TopicSyncRequest(String id, String title, String description, List<String> links, int stage, String lastRevisionDate, String nextRevisionDate, String updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.links = links;
        this.stage = stage;
        this.lastRevisionDate = lastRevisionDate;
        this.nextRevisionDate = nextRevisionDate;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getLinks() { return links; }
    public int getStage() { return stage; }
    public String getLastRevisionDate() { return lastRevisionDate; }
    public String getNextRevisionDate() { return nextRevisionDate; }
    public String getUpdatedAt() { return updatedAt; }
}