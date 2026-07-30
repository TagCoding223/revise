package com.revise.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRequest {
    @NotBlank
    private String refreshToken;
}