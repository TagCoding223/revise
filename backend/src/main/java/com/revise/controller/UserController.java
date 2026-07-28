package com.revise.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.revise.dto.request.CreateUserRequest;
import com.revise.dto.request.SetPasswordRequest;
import com.revise.dto.response.ApiResponse;
import com.revise.dto.response.UserResponse;
import com.revise.exception.UnauthorizedException;
import com.revise.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// block all endpoints except set-password endpoint
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor // what is done and how it is allow to access userServiceImpl class method to link with final userService field
public class UserController {

    private final UserService userService;
    
    @GetMapping("/test")
    public String test() {
        return new String("Worked");
    }
    
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        UserResponse createdUser = userService.createUser(request);
        return new ResponseEntity<>(createdUser,HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        // Define the Cache-Control rule
        CacheControl cacheRules = CacheControl.noStore();
        return ResponseEntity.ok().cacheControl(cacheRules).body(userService.getUserById(id));
    }
    
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        // Define the Cache-Control rule
        CacheControl cacheRules = CacheControl.noStore();
        return ResponseEntity.ok().cacheControl(cacheRules).body(userService.getAllUsers());
    }

    @PostMapping("/set-password")
    public ResponseEntity<ApiResponse> setPassword(@Valid @RequestBody SetPasswordRequest request, Principal principal) {
        // Security check: Ensure the request contains a valid JWT
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to set password");
        }
        
        return ResponseEntity.ok(userService.setPassword(principal.getName(), request.getPassword()));
    }
    
}
