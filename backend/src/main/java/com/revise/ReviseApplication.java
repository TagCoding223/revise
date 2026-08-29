package com.revise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class ReviseApplication {

    // 1. CRITICAL FIX: The static block executes immediately upon class load.
    // This guarantees UTC is locked in for both normal server runs and JUnit tests,
    // well before Hibernate or Jackson initialize.
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

	public static void main(String[] args) {
        // CRITICAL FIX: Lock the timezone before Spring Boot initializes its components!
        // TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(ReviseApplication.class, args);
	}
}
