package com.revise.dto.request;

import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String fullName;
    private String password;
}
