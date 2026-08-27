package com.revise.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class TopicSyncRequest {
    // We expect the Android app (Room Database) to generate the UUID 
    // so we can link offline creations to the backend smoothly.
    private String id; 
    
    private String title;
    private String description;
    private List<String> links;
    private int stage;
    
    private LocalDateTime lastRevisionDate;
    private LocalDateTime nextRevisionDate;
    private boolean deleted;
    
    // Crucial for the "Last Write Wins" conflict resolution
    private LocalDateTime updatedAt;
}
