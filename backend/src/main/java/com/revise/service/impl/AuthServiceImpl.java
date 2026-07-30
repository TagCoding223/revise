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
import com.revise.dto.request.TokenRefreshRequest;
import com.revise.dto.response.AuthResponse;
import com.revise.dto.response.UserResponse;
import com.revise.entity.RefreshToken;
import com.revise.entity.User;
import com.revise.entity.UserCredential;
import com.revise.entity.VerificationCode;
import com.revise.exception.InvalidOtpException;
import com.revise.exception.ResourceNotFoundException;
import com.revise.exception.UnauthorizedException;
import com.revise.exception.UserAlreadyExistsException;
import com.revise.exception.UserNotVerifiedException;
import com.revise.repository.RefreshTokenRepository;
import com.revise.repository.UserCredentialRepository;
import com.revise.repository.UserRepository;
import com.revise.repository.VerificationCodeRepository;
import com.revise.security.JwtTokenProvider;
import com.revise.service.AuthService;
import com.revise.service.OtpService;
import com.revise.service.RefreshTokenService;
import com.revise.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    private final UserService userService;
    private final UserCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final VerificationCodeRepository otpRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

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

        // 3. Create the Credentials 
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

        // 5. Return flow response (NO TOKEN ISSUED HERE)
        AuthResponse response = new AuthResponse();
        response.setMessage("Signup successful. Please verify OTP.");
        response.setRefreshToken(refreshTokenService.createRefreshToken(createdUser.getId()).getToken());
        response.setUserId(createdUser.getId());
        return response;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. Fetch user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email."));

        // 2. Fetch credentials. If missing, they signed up with Google.
        UserCredential credential = credentialRepository.findById(user.getId())
                .orElseThrow(() -> new UnauthorizedException("No password set for this account. Please log in with Google."));

        // 3. Verify password FIRST. This prevents attackers from spamming OTPs.
        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            throw new UnauthorizedException("Incorrect password.");
        }

        // 4. If password is correct but email is unverified, trigger OTP resend and block login.
        if (!user.isEmailVerified()) {
            otpService.generateAndSendOtp(user.getEmail());
            throw new UserNotVerifiedException("Email unverified. A new OTP has been sent.");
        }

        // 5. Generate the JWT token
        String token = jwtTokenProvider.generateToken(user.getId());

        AuthResponse response = new AuthResponse();
        response.setMessage("Login successful.");
        response.setToken(token);
        response.setRefreshToken(refreshTokenService.createRefreshToken(user.getId()).getToken());
        response.setUserId(user.getId());
        return response;
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(String email, String otp) {
        // 1. Fetch the OTP record 
        VerificationCode storedCode = otpRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No active OTP found for this email."));

        // 2. Check expiration 
        if(storedCode.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new InvalidOtpException("Your OTP has expired. Please request a new one.");
        }

        // 3. Check if the code matches 
        if (!storedCode.getCode().equals(otp)) {
            throw new InvalidOtpException("Invalid verification code. Please try again.");
        }

        // 4. Mark user as verified 
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found in the system."));

        user.setEmailVerified(true);
        userRepository.save(user);

        // 5. Clean up the used OTP
        otpRepository.delete(storedCode);

        // 6. Generate the JWT Token now that they are verified
        String token = jwtTokenProvider.generateToken(user.getId());

        // 7. Return success response with Token
        AuthResponse response = new AuthResponse();
        response.setMessage("Email verified successfully!");
        response.setToken(token);
        response.setRefreshToken(refreshTokenService.createRefreshToken(user.getId()).getToken());
        response.setUserId(user.getId());
        return response;
    }
    
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
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

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

            if (existingUserOpt.isPresent()) {
                user = existingUserOpt.get();
                
                // If they previously signed up manually but never verified, Google verifies them now.
                if (!user.isEmailVerified()) {
                    user.setEmailVerified(true); 
                    userRepository.save(user);
                }
            } else {
                // 5. Create a new user if they don't exist
                CreateUserRequest userReq = new CreateUserRequest();
                userReq.setEmail(email);
                userReq.setFullName(name);
                userReq.setAuthProvider("GOOGLE"); 

                UserResponse createdUserResponse = userService.createUser(userReq);
                
                user = userRepository.findById(createdUserResponse.getId())
                        .orElseThrow(() -> new RuntimeException("Failed to retrieve created user"));
                
                user.setEmailVerified(true);
                userRepository.save(user);
            }

            // 6. Generate our custom JWT for the React frontend
            String jwt = jwtTokenProvider.generateToken(user.getId());

            // 7. Return the standard auth response
            AuthResponse response = new AuthResponse();
            response.setMessage("Google authentication successful.");
            response.setToken(jwt);
            response.setUserId(user.getId());
            response.setRefreshToken(refreshTokenService.createRefreshToken(user.getId()).getToken());
            
            return response;

        } catch (Exception e) {
            throw new UnauthorizedException("Google authentication failed: " + e.getMessage());
        }
    }

    @Override
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        return refreshTokenRepository.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // Generate a fresh JWT Access Token
                    String token = jwtTokenProvider.generateToken(user.getId());
                    
                    AuthResponse response = new AuthResponse();
                    response.setMessage("Token refreshed successfully.");
                    response.setToken(token);
                    response.setRefreshToken(request.getRefreshToken()); // Send back the same valid refresh token
                    response.setUserId(user.getId());
                    return response;
                })
                .orElseThrow(() -> new UnauthorizedException("Refresh token is not in database!"));
    }
}