package com.revise;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZoneId;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ReviseApplicationTests {

    @Test
    void contextLoads() {
        // Ensures the Spring context loads successfully without crashing
    }

    @Test
    void testTimezoneIsLockedToUTC() {
        // Act
        String defaultTimezone = TimeZone.getDefault().getID();

        // Assert
        assertEquals("UTC", defaultTimezone, 
            "CRITICAL: The application default timezone must be locked to UTC to prevent mobile sync conflicts.");
    }

    @Test
    void testSystemDefaultZoneIdIsUTC() {
        // Act
        String defaultZoneId = ZoneId.systemDefault().getId();

        // Assert
        assertEquals("UTC", defaultZoneId, 
            "CRITICAL: The ZoneId must be UTC so LocalDateTime.now() generates correct server timestamps.");
    }
}