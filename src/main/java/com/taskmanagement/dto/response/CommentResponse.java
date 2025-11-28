package com.taskmanagement.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private UserResponse author;
    private Long taskId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
