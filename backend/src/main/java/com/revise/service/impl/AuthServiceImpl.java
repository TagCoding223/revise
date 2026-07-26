package com.revise.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revise.dto.request.CreateUserRequest;
import com.revise.dto.request.LoginRequest;
import com.revise.dto.request.SignupRequest;
import com.revise.dto.response.AuthResponse;
import com.revise.dto.response.UserResponse;
import com.revise.entity.User;
import com.revise.entity.UserCredential;
import com.revise.entity.VerificationCode;
import com.revise.exception.InvalidOtpException;
import com.revise.exception.ResourceNotFoundException;
import com.revise.exception.UnauthorizedException;
import com.revise.exception.UserAlreadyExistsException;
import com.revise.repository.UserCredentialRepository;
import com.revise.repository.UserRepository;
import com.revise.repository.VerificationCodeRepository;
import com.revise.security.JwtTokenProvider;
import com.revise.service.AuthService;
import com.revise.service.OtpService;
import com.revise.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    // Injecting UserService to handle the profile side of things
    private final UserService userService;
    private final UserCredentialRepository credentialRepository;

    // Injecting the PasswordEncoder bean
    private final PasswordEncoder passwordEncoder;

    // Injecting the UserRepository for the existence check
    private final UserRepository userRepository;

    private final OtpService otpService;
    private final VerificationCodeRepository otpRepository;

    // Inject the JWT provider
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // 1. Check if user exists. If yes, throw exception.
        if(userRepository.existsByEmail(request.getEmail())){
            throw new UserAlreadyExistsException("You already have an account. Please log in.");
        }

        // 2. Create the base User profile using the UserService
        CreateUserRequest userReq = new CreateUserRequest();
        userReq.setEmail(request.getEmail());
        userReq.setFullName(request.getFullName());
        userReq.setAuthProvider("LOCAL");

        UserResponse createdUser = userService.createUser(userReq);

        // 3. Create the Credentials (we will hash the password later)
        User userReference = new User();
        userReference.setId(createdUser.getId());

        UserCredential credential = new UserCredential();
        credential.setUser(userReference);

        // Hash the password before saving
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        credential.setPasswordHash(hashedPassword);

        credentialRepository.save(credential);

        // 4. Generate and send OTP via email.
        otpService.generateAndSendOtp(request.getEmail());

        // 5. Return flow response
        AuthResponse response = new AuthResponse();
        response.setMessage("Signup successful. Please verify OTP.");
        response.setUserId(createdUser.getId());
        return response;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. Fetch user by email
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResourceNotFoundException("No account found with this email."));

        // 2. Ensure they verified their email
        if(!user.isEmailVerified()){
            throw new UnauthorizedException("Please verify your email before logging in.");
        }

        // 3. Fetch credentials and verify password
        UserCredential credential = credentialRepository.findById(user.getId()).orElseThrow(() -> new ResourceNotFoundException("Credentials missing."));

        if(!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())){
            throw new UnauthorizedException("Incorrect password.");
        }

        // 4. Generate Jwt token
        String token = jwtTokenProvider.generateToken(user.getId());

        // 5. Return the payload
        AuthResponse response = new AuthResponse();
        response.setMessage("Login successful.");
        response.setToken(token);
        response.setUserId(user.getId());
        return response;
    }

    @Override
    public AuthResponse verifyOtp(String email, String otp) {
        // 1. Fetch the OTP record (Throws 404 if missing)
        VerificationCode storedCode = otpRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("No active OTP found for this email."));

        // 2. Check expiration (Throw 400 if expired)
        if(storedCode.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new InvalidOtpException("Your OTP has expired. Please request a new one.");
        }

        // 3. Check if the code matches (Throws 400 if wrong)
        if (!storedCode.getCode().equals(otp)) {
            throw new InvalidOtpException("Invalid verification code. Please try again.");
        }

        // 4. Mark user as verified (Throws 404 if user somehow vanished)
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found in the system."));

        user.setEmailVerified(true);
        userRepository.save(user);

        // 5. Clean up the used OTP
        otpRepository.delete(storedCode);

        // 6. Jwt Token generate
        String token = jwtTokenProvider.generateToken(user.getId());

        // 7. Return success response
        AuthResponse response = new AuthResponse();
        response.setMessage("Email verified successfully!");
        response.setToken(token);
        response.setUserId(user.getId());
        return response;
    }
    
    // Method to handle the Resend button on the frontend
    @Override
    public AuthResponse resendOtp(String email){
        otpService.generateAndSendOtp(email);

        AuthResponse response = new AuthResponse();
        response.setMessage("A new OTP has been sent to your email.");
        return response;
    }
}
