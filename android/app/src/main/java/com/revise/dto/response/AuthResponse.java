package com.revise.dto.response;

public class AuthResponse {
    private String message;
    private String token;
    private String refreshToken;
    private String userId;
    private boolean newUser;

    // Getters and Setters
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public String getUserId() { return userId; }
    public boolean isNewUser() { return newUser; }
    public String getRefreshToken() { return refreshToken; }
}
