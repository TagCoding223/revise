package com.revise.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.revise.dto.request.CreateUserRequest;
import com.revise.dto.response.UserResponse;
import com.revise.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    
    @GetMapping("/test")
    public String test() {
        return new String("Worked");
    }
    
    @PostMapping("/createUser")
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        UserResponse createdUser = userService.createUser(request);
        return new ResponseEntity<>(createdUser,HttpStatus.CREATED);
    }
    
}
