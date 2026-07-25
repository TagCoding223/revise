package com.revise.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revise.entity.VerificationCode;
import com.revise.repository.VerificationCodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {
    
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;

    @Transactional
    public void generateAndSendOtp(String email){
        // 1. Delete any existing code for this email so they don't pile up
        verificationCodeRepository.deleteByEmail(email);

        // 2. Generate a 4-digit random number (e.g., "0492", "8319")
        String otp = String.format("%04d", new Random().nextInt(10000));

        // 3. Save to database with a 2-minute expiration
        VerificationCode codeEntity = new VerificationCode();
        codeEntity.setEmail(email);
        codeEntity.setCode(otp);
        codeEntity.setExpiresAt(LocalDateTime.now().plusMinutes(2));

        verificationCodeRepository.save(codeEntity);

        // 4. Send the email
        emailService.sendOtpEmail(email, otp);
    }
}
