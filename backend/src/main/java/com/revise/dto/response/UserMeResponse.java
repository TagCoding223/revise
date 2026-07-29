package com.revise.dto.response;

import lombok.Data;

@Data
public class UserMeResponse {
    String fullName;
    String email;
    boolean emailVerified;   
}
