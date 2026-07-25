package com.revise.service.impl;

import org.springframework.stereotype.Service;

import com.revise.dto.request.CreateUserRequest;
import com.revise.dto.request.LoginRequest;
import com.revise.dto.request.SignupRequest;
import com.revise.dto.response.AuthResponse;
import com.revise.dto.response.UserResponse;
import com.revise.entity.User;
import com.revise.entity.UserCredential;
import com.revise.repository.UserCredentialRepository;
import com.revise.service.AuthService;
import com.revise.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    // Injecting UserService to handle the profile side of things
    private final UserService userService;
    private final UserCredentialRepository credentialRepository;

    @Override
    public AuthResponse signup(SignupRequest request) {
        // TODO: 1. (Future) Check if user exists. If yes, throw exception.

        // 2. Create the base User profile using the UserService
        CreateUserRequest userReq = new CreateUserRequest();
        userReq.setEmail(request.getEmail());
        userReq.setFullName(request.getFullName());
        userReq.setAuthProvider("LOCAL");

        UserResponse createdUser = userService.createUser(userReq);

        // 3. Create the Credentials (we will hash the password later)
        User userReference = new User();
        userReference.setId(createdUser.getId());

        UserCredential credential = new UserCredential();
        credential.setUser(userReference);
        credential.setPasswordHash(request.getPassword()); // TODO: Apply BCrypt hashing here

        credentialRepository.save(credential);

        // TODO: 4. (Future) Generate and send OTP via email.

        // 5. Return flow response
        AuthResponse response = new AuthResponse();
        response.setMessage("Signup successful. Please verify OTP.");
        response.setUserId(createdUser.getId());
        return response;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Basic flow stub: We will add real validation and JWT generation later
        AuthResponse response = new AuthResponse();
        response.setMessage("Login flow reached for: "+ request.getEmail());
        response.setToken("dummy-jwt-token-for-now");
        return response;
    }

    @Override
    public AuthResponse verifyOtp(String email, String otp) {
        // Basic flow stub
        AuthResponse response = new AuthResponse();
        response.setMessage("OTP verified for: "+ email);
        response.setToken("dummy-jwt-token-for-now");
        return response;
    }
    
}
