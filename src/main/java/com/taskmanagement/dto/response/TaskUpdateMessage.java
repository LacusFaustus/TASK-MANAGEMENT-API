package com.taskmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateMessage {
    private Long taskId;
    private String updateType; // CREATED, UPDATED, DELETED, STATUS_CHANGED
    private Long timestamp;
    private Object data;
}
