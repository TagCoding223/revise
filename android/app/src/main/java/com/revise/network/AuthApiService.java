package com.revise.network;

import com.revise.dto.request.LoginRequest;
import com.revise.dto.request.SignupRequest;
import com.revise.dto.response.AuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("api/v1/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/v1/auth/signup")
    Call<AuthResponse> signup(@Body SignupRequest request);
}
