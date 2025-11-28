package com.taskmanagement.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private Boolean emailVerified;
    private Integer workspaceCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
