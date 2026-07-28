package com.revise.dto.response;

import lombok.Data;

@Data
public class AuthResponse {
    private String message;
    private String token; 
    private String userId;

    // Identifies if the user was just created in the database
    private boolean newUser; // by default false
}
