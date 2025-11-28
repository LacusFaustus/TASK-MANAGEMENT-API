package com.taskmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUserResponse {
    private Long userId;
    private String userName;
    private String status; // ONLINE, AWAY, OFFLINE
    private Long lastActive;
}
