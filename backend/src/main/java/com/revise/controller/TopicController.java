package com.revise.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/topic")
public class TopicController {
    
    @GetMapping("/testSecureRoute")
    public String test() {
        return new String("Secure Route"); // if JWT Security Filter not implement then spring security throw 403 Forbidden: The server knows exactly who you are, but you lack the required account privileges or roles. Re-authenticating will not fix it. with and while passing jwt token in request barrier but Spring Security, by default, does not know what a JWT is. It expects traditional session cookies. Therefore, we must create a custom filter that intercepts every incoming HTTP request
    }
    
}
