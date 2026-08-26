package com.revise.model;

import java.util.List;

public class Topic {
    private String id;
    private String title;
    private String description;
    private int stage;
    private String category; // "today", "tomorrow", "other"
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
}
