package com.revise.dto.response;

import lombok.Data;

@Data
public class AuthResponse {
    private String message;
    private String token; // We will populate this with a JWT later
    private String userId;
}
