package com.revise.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TopicRequest {
    
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    // Accepts the dynamic array of reference links from your frontend
    private List<String> links;
}
