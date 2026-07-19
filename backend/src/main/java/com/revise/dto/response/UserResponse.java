package com.revise.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String email;
    private String fullName;
    private String authProvider;
    private LocalDateTime createdAt;
}
