package com.revise;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class ResendEmailTest {

    /**
     * SAFE PIPELINE TEST: 
     * Verifies that the version bump in pom.xml didn't break the SDK's 
     * core classes, methods, or builder patterns. This requires no API key.
     */
    @Test
    void testResendSdkBuilderCompilesAndMapsCorrectly() {
        // Arrange & Act
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("onboarding@resend.dev")
                .to("test@example.com")
                .subject("Dependency Version Test")
                .html("<p>Testing Resend Java SDK update</p>")
                .build();

        // Assert
        assertNotNull(params, "CreateEmailOptions object should be successfully built");
        assertEquals("test@example.com", params.getTo().get(0), "Recipient email should map correctly");
        assertEquals("Dependency Version Test", params.getSubject(), "Subject should map correctly");
    }

    /**
     * LIVE NETWORK TEST:
     * This actually fires a request to the Resend API to verify your account and network.
     * It is marked @Disabled so it doesn't accidentally run (and fail) in GitHub Actions 
     * where the real API key is missing.
     */
    @Test
    @Disabled("Remove the @Disabled annotation temporarily to test live sending locally")
    void testLiveEmailSending() {
        // Arrange: Replace with your actual Resend API Key and verified email address
        String resendApiKey = "re_YOUR_ACTUAL_API_KEY_HERE";
        String verifiedSender = "onboarding@resend.dev"; // Or your custom verified domain
        String yourPersonalEmail = "your.email@example.com";

        Resend resend = new Resend(resendApiKey);
        
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(verifiedSender)
                .to(yourPersonalEmail)
                .subject("Production Readiness Test")
                .html("<strong>Success!</strong> The updated Resend dependency is working perfectly.")
                .build();

        // Act & Assert
        try {
            CreateEmailResponse data = resend.emails().send(params);
            assertNotNull(data.getId(), "Email ID should not be null if the API successfully accepted the request");
            System.out.println("Email sent successfully! ID: " + data.getId());
        } catch (ResendException e) {
            fail("Live email sending failed due to API or network error: " + e.getMessage());
        }
    }
}