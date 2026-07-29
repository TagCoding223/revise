package com.revise.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.stream.Collectors;
import com.revise.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    // Handle bad password (UnauthorizedException)
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse> handleUnauthorizedException(UnauthorizedException ex){
        ApiResponse response = new ApiResponse(false, ex.getMessage());
        return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED); //401
    }

    // Keep this at the bottom to catch actual server crashes
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneralException(Exception ex){
        log.error(ex.getMessage(), ex);
        ApiResponse response = new ApiResponse(false, "An unexpected internal server error occurred.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex){
        // Extract all the default messages we wrote in the DTO and join them into a single string
        String errorMessage = ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(" | "));

        // Wrap it in our standard ApiResponse format
        ApiResponse response = new ApiResponse(false, errorMessage);

        // Return 400 Bad Request
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotVerifiedException.class)
    public ResponseEntity<ApiResponse> handleUserNotVerified(UserNotVerifiedException ex) {
        // 403 Forbidden is the standard status for "Authenticated but lacking required status (verification)"
        return new ResponseEntity<>(new ApiResponse(false, ex.getMessage()), HttpStatus.FORBIDDEN); 
    }
}
