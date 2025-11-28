package com.taskmanagement.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private String firstName;
    private String lastName;

    @Email
    private String email;
}
