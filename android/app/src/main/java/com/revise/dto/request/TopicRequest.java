package com.revise.dto.request;

import java.util.List;

public class TopicRequest {
    private String title;
    private String description;
    private List<String> links;

    public TopicRequest(String title, String description, List<String> links) {
        this.title = title;
        this.description = description;
        this.links = links;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getLinks() { return links; }
    public void setLinks(List<String> links) { this.links = links; }
}