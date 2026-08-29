package com.revise;

import org.junit.jupiter.api.Test;
import java.time.ZoneId;
import java.util.TimeZone;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TimezoneConfigTest {

    @Test
    void testTimezoneIsLockedToUTC() throws ClassNotFoundException {
        // Trigger the static block in your main class to load
        Class.forName("com.revise.ReviseApplication"); 

        String defaultTimezone = TimeZone.getDefault().getID();
        assertEquals("UTC", defaultTimezone, 
            "CRITICAL: The application default timezone must be locked to UTC.");
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