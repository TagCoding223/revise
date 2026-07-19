package com.revise.dto.request;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String email;
    private String fullName;
    private String authProvider;
}
