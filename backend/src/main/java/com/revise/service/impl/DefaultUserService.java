package com.revise.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revise.dto.request.CreateUserRequest;
import com.revise.dto.response.ApiResponse;
import com.revise.dto.response.UserMeResponse;
import com.revise.dto.response.UserResponse;
import com.revise.entity.User;
import com.revise.entity.UserCredential;
import com.revise.exception.ResourceNotFoundException;
import com.revise.repository.UserCredentialRepository;
import com.revise.repository.UserRepository;
import com.revise.service.UserService;

import lombok.RequiredArgsConstructor;

// Never use the suffix Impl on your classes. It is an anti-pattern because it leaks implementation details. Name your implementation based on its specific domain, such as UserService (interface) and DatabaseUserService or DefaultUserService (class).
// so i change the UserServiceImpl to DefaultUserService (to define general operation related to user and authentication in AuthUserService)

@Service
@RequiredArgsConstructor
public class DefaultUserService implements UserService{

    private final UserRepository userRepository;
    private final UserCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        // 1. Map DTO to Entity
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setAuthProvider(request.getAuthProvider() != null ? request.getAuthProvider() : "LOCAL");
        
        // 2. Save to Database via Repository
        User savedUser = userRepository.save(user);

        // 3. Map Entity back to Response DTO
        return mapToResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: "+ id));

        return mapToResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    
    // Helper method to keep mapping logic clean
    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setAuthProvider(user.getAuthProvider());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    @Override
    @Transactional
    public ApiResponse setPassword(String userId, String rawPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Fetch existing credentials or create a new row if they don't have one (like Google users)
        UserCredential credential = credentialRepository.findById(userId)
                .orElse(new UserCredential());
                
        if (credential.getUser() == null) {
            credential.setUser(user);
        }

        credential.setPasswordHash(passwordEncoder.encode(rawPassword));
        credentialRepository.save(credential);

        return new ApiResponse(true, "Password set successfully.");
    }

    @Override
    public UserMeResponse getMeUserById(String id){
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserMeResponse meUser = new UserMeResponse();
        meUser.setEmail(user.getEmail());
        meUser.setFullName(user.getFullName());
        meUser.setEmailVerified(user.isEmailVerified());

        return meUser;
    }
}
