package com.revise.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.revise.dto.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // This catches our custom exception and returns a 409 Conflict status
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleUserAlreadyExists(UserAlreadyExistsException ex){
        ApiResponse response = new ApiResponse(false, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT); // 409
    }

    // Handle bad OTP submission
    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiResponse> handleInvalidOtp(InvalidOtpException ex){
        ApiResponse response = new ApiResponse(false, ex.getMessage());
        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST); // 400
    }

    // Handle missing records (User or OTP)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFound(ResourceNotFoundException ex){
        ApiResponse response = new ApiResponse(false, ex.getMessage());
        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND); // 404
    }

    // Keep this at the bottom to catch actual server crashes
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneralException(Exception ex){
        log.error(ex.getMessage(), ex);
        ApiResponse response = new ApiResponse(false, "An unexpected internal server error occurred.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500
    }
}
