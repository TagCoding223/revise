package com.revise.service;

import com.revise.dto.request.CreateUserRequest;
import com.revise.dto.response.ApiResponse;
import com.revise.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(String id);

    List<UserResponse> getAllUsers(); // remove or admin secure this

    ApiResponse setPassword(String userId, String rawPassword);
}
