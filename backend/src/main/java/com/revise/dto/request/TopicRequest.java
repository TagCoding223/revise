package com.revise.dto.request;

import java.util.List;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TopicRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    // Limits the description to prevent database overflow
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")    
    private String description;

    // Accepts the dynamic array of reference links from your frontend
    // 1. Validates the list size (max 10 links)
    // 2. Validates each individual string inside the list to ensure it is a valid web address
    @Size(max = 10, message = "You can only add up to 10 reference links per topic")
    private List<@URL(message = "One or more reference links are invalid URLs") String> links;
}
