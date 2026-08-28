package com.revise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReviseApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReviseApplication.class, args);
	}

	// Forces the entire backend to run in UTC
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
