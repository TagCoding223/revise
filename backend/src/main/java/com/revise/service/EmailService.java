package com.revise.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {
    private final Resend resend;
    private final String fromEmail;

    // Injects the key from application.properties
    public EmailService(@Value("${resend.api.key}") String apiKey, @Value("${from.email.address}") String fromEmail){
        this.resend = new Resend(apiKey);
        this.fromEmail = fromEmail;
    }

    public void sendOtpEmail(String toEmail, String otpCode){
        CreateEmailOptions sendEmailRequest = CreateEmailOptions.builder()
        .from("Revise App "+fromEmail) // Use your verified Resend domain in production
        .to(toEmail)
        .subject("Your Revise Verification Code")
        .html("<h2>Welcome to Revise!</h2><p>Your 4-digit verification code is: <strong>"+otpCode+"</strong></p><p>This code will expire in 10 minutes.</p>")
        .build();

        try {
            resend.emails().send(sendEmailRequest);
            log.info("OTP Email sent to: "+ toEmail);
        } catch (ResendException e) {
            log.warn("Failed to send email: "+e.getMessage());
            throw new RuntimeException("Email sending failed");
        }
    }
}
