package com.revise.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.revise.dto.request.TopicRequest;
import com.revise.dto.response.ApiResponse;
import com.revise.dto.response.TopicResponse;
import com.revise.service.TopicService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;
import java.util.TimeZone;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.revise.dto.request.TopicSyncRequest;


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
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteTopic(@PathVariable String id, Principal principal){
        return ResponseEntity.ok(topicService.deleteTopic(id, principal.getName()));
    }

    @PatchMapping("/{id}/revise")
    public ResponseEntity<TopicResponse> markTopicAsRevised(@PathVariable String id, Principal principal){
        // PATCH is used here instead of PUT because we are only partially updating 
        // the resource (advancing the stage/dates) rather than replacing the whole entity.
        return ResponseEntity.ok(topicService.markTopicAsRevised(id, principal.getName()));
    }
    
    @PatchMapping("/revise-today")
    public ResponseEntity<ApiResponse> reviseAllToday(Principal principal) {
        // Triggers the optimized bulk database update strictly for the authenticated user
        return ResponseEntity.ok(topicService.reviseAllToday(principal.getName()));
    }

    // --- MOBILE SYNC ENDPOINTS ---

    /**
     * PULL SYNC: Mobile app requests topics updated since its last sync time.
     * Expects an ISO Date String: ?since=2026-07-29T10:15:30
     */
    @GetMapping("/sync")
    public ResponseEntity<?> pullSync(
            @RequestParam("since") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since, 
            Principal principal) {
        try {
            List<TopicResponse> updatedTopics = topicService.pullSync(principal.getName(), since);
            // 200 OK: Returns the array of topics
            return ResponseEntity.ok(updatedTopics); 
            
        } catch (Exception e) {
            // 500 Internal Server Error: Tells Android WorkManager to retry this task later
            ApiResponse errorResponse = new ApiResponse(false, "Failed to pull data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * PUSH SYNC: Mobile app sends a batch of topics modified while offline.
     */
    @PostMapping("/sync/batch")
    public ResponseEntity<ApiResponse> pushSync(
            @RequestBody List<TopicSyncRequest> offlineTopics, 
            Principal principal) {
        
        // Prevent empty batch requests from wasting server resources
        if (offlineTopics == null || offlineTopics.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "No topics provided for sync."));
        }

        try {
            ApiResponse response = topicService.pushSync(principal.getName(), offlineTopics);
            // 200 OK: Sync successful
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // 500 Internal Server Error: Tells Android WorkManager to retry this task later
            ApiResponse errorResponse = new ApiResponse(false, "Batch sync failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/testSecureRoute")
    public String test() {
        String defaultZoneId = ZoneId.systemDefault().getId();
        System.out.println(defaultZoneId);
        String defaultTimezone = TimeZone.getDefault().getID();
        System.out.println(defaultTimezone);
        return new String(defaultTimezone+" "+defaultZoneId);
        // if JWT Security Filter not implement then spring security throw 403 Forbidden: The server knows exactly who you are, but you lack the required account privileges or roles. Re-authenticating will not fix it. with and while passing jwt token in request barrier but Spring Security, by default, does not know what a JWT is. It expects traditional session cookies. Therefore, we must create a custom filter that intercepts every incoming HTTP request
    }
    
}
