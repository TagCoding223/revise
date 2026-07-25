package com.revise.entity;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.revise.dto.response.ApiResponse;
import com.revise.exception.UserAlreadyExistsException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // This catches our custom exception and returns a 409 Conflict status
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleUserAlreadyExists(UserAlreadyExistsException ex){
        ApiResponse response = new ApiResponse(false, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    public ResponseEntity<ApiResponse> handleGeneralException(Exception ex){
        ApiResponse response = new ApiResponse(false, "An unexpected error occurred.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
