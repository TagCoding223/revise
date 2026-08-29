package com.revise.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class TopicResponse {
    private String id;
    private String title;
    private String description;
    private List<String> links;
    private int stage;
    private LocalDateTime lastRevisionDate;
    private LocalDateTime nextRevisionDate;
    private boolean deleted;
    private LocalDateTime updatedAt;

    // We will calculate this dynamically in the service layer to make frontend filtering easier
    private String category;
}
