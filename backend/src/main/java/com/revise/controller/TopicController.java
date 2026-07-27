package com.revise.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.revise.dto.request.TopicRequest;
import com.revise.dto.response.TopicResponse;
import com.revise.service.TopicService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @PostMapping
    public ResponseEntity<TopicResponse> createTopic(@Valid @RequestBody TopicRequest request, Principal principal) {
        // principal.getName() securely returns the userId from the JWT token
        TopicResponse createdTopic = topicService.createTopic(request, principal.getName());
        return new ResponseEntity<>(createdTopic, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TopicResponse> updateTopic(@PathVariable String id, @Valid @RequestBody TopicRequest request, Principal principal) {
        return ResponseEntity.ok(topicService.updateTopic(id, request, principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TopicResponse> getTopicById(@PathVariable String id, Principal principal) {
        return ResponseEntity.ok(topicService.getTopicById(id, principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<TopicResponse>> getAllTopics(Principal principal) {
        return ResponseEntity.ok(topicService.getAllTopicForUser(principal.getName()));
    }
    
    
    
    @GetMapping("/testSecureRoute")
    public String test() {
        return new String("Secure Route"); // if JWT Security Filter not implement then spring security throw 403 Forbidden: The server knows exactly who you are, but you lack the required account privileges or roles. Re-authenticating will not fix it. with and while passing jwt token in request barrier but Spring Security, by default, does not know what a JWT is. It expects traditional session cookies. Therefore, we must create a custom filter that intercepts every incoming HTTP request
    }
    
}
