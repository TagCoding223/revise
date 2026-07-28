package com.revise.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.revise.dto.request.CreateUserRequest;
import com.revise.dto.request.GoogleAuthRequest;
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

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

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
    // TODO: Add Validation for form data like password should be 8 for signup and login
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

    @Override
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        try {
            // 1. Initialize the Google Token Verifier
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory()
            ).setAudience(Collections.singletonList(googleClientId)).build();

            // 2. Verify the token provided by the frontend
            GoogleIdToken idToken = verifier.verify(request.getIdToken());

            if (idToken == null) {
                throw new UnauthorizedException("Invalid Google ID Token.");
            }

            // 3. Extract user payload from the Google Token
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // 4. Check if user already exists in our database
            Optional<User> existingUserOpt = userRepository.findByEmail(email);
            User user;
            boolean isNewUser = false; // Track the user state

            if(existingUserOpt.isPresent()){
                user = existingUserOpt.get();
                // Optional: If they previously signed up with LOCAL, you might want to update their auth provider to "GOOGLE" or handle linking accounts here.
                user.setAuthProvider("GOOGLE");
                user.setEmailVerified(true); // Google emails are implicitly verified
                userRepository.save(user);
            } else{
                // 5. Create a new user if they don't exist
                CreateUserRequest userReq = new CreateUserRequest();
                userReq.setEmail(email);
                userReq.setFullName(name);
                userReq.setAuthProvider("GOOGLE"); // Mark as Google user

                UserResponse createdUserResponse = userService.createUser(userReq);

                // Fetch the newly created entity so we can mark it as verified
                user = userRepository.findById(createdUserResponse.getId())
                        .orElseThrow(() -> new RuntimeException("Failed to retrieve created user"));
                
                user.setEmailVerified(true);
                userRepository.save(user);
                
                // Note: We DO NOT create a UserCredential row here, because Google users don't have a local password to hash.

                isNewUser = true; // Flag them as a new user
            }

            // 6. Generate our custom JWT for the React frontend
            String jwt = jwtTokenProvider.generateToken(user.getId());

            // 7. Return the standard auth response
            AuthResponse response = new AuthResponse();
            response.setMessage("Google authentication successful.");
            response.setToken(jwt);
            response.setUserId(user.getId());
            response.setNewUser(isNewUser);
            return response;
        } catch (Exception e) {
            // If the token is expired, tampered with, or network fails
            throw new UnauthorizedException("Google authentication failed: " + e.getMessage());
        }
    }
}
