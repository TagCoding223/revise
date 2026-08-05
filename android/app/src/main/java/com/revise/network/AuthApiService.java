package com.revise.network;

import com.revise.dto.request.GoogleAuthRequest;
import com.revise.dto.request.LoginRequest;
import com.revise.dto.request.RefreshRequest;
import com.revise.dto.request.SignupRequest;
import com.revise.dto.response.AuthResponse;
import com.revise.dto.response.TokenRefreshResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthApiService {

    @POST("api/v1/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/v1/auth/signup")
    Call<AuthResponse> signup(@Body SignupRequest request);

    @POST("api/v1/auth/refresh")
    Call<TokenRefreshResponse> refreshToken(@Body RefreshRequest request);

    @POST("api/v1/auth/google")
    Call<AuthResponse> googleLogin(@Body GoogleAuthRequest request);

    @POST("api/v1/auth/verify-otp")
    Call<AuthResponse> verifyOtp(
            @Query("email") String email,
            @Query("otp") String otp
    );

    @POST("api/v1/auth/resend-otp")
    Call<AuthResponse> resendOtp(
            @Query("email") String email
    );
}
