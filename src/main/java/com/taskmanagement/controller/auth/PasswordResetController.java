package com.taskmanagement.controller.auth;

import com.taskmanagement.dto.request.PasswordResetConfirmRequest;
import com.taskmanagement.dto.request.PasswordResetRequest;
import com.taskmanagement.dto.response.ApiResponse;
import com.taskmanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/password")
@RequiredArgsConstructor
@Tag(name = "Password Reset", description = "API для сброса пароля")
public class PasswordResetController {

    private final AuthService authService;

    @PostMapping("/forgot")
    @Operation(summary = "Запрос на сброс пароля")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Password reset instructions sent to your email"));
    }

    @PostMapping("/reset")
    @Operation(summary = "Сброс пароля с токеном")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }
}

