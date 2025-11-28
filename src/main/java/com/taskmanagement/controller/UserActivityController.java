package com.taskmanagement.controller;

import com.taskmanagement.dto.request.UserActivityRequest;
import com.taskmanagement.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-activities")
@RequiredArgsConstructor
public class UserActivityController {

    private final UserActivityService userActivityService;

    @PostMapping("/record")
    public ResponseEntity<String> recordActivity(@RequestBody UserActivityRequest request) {
        userActivityService.recordUserActivity(
                request.getUserId(),
                request.getActivityType(),
                request.getEntityId()
        );
        return ResponseEntity.ok("Activity recorded");
    }
}
