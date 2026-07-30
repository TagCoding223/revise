package com.revise.service;

import com.revise.dto.request.GoogleAuthRequest;
import com.revise.dto.request.LoginRequest;
import com.revise.dto.request.SignupRequest;
import com.revise.dto.request.TokenRefreshRequest;
import com.revise.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse signup(SignupRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse verifyOtp(String email, String otp);
    AuthResponse resendOtp(String email);
    AuthResponse googleAuth(GoogleAuthRequest request);
    AuthResponse refreshToken(TokenRefreshRequest request);
}
