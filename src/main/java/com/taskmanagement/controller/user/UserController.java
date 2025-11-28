package com.taskmanagement.controller.user;

import com.taskmanagement.dto.request.UserUpdateRequest;
import com.taskmanagement.dto.response.ApiResponse;
import com.taskmanagement.dto.response.UserProfileResponse;
import com.taskmanagement.dto.response.UserStatsResponse;
import com.taskmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "API для управления профилем пользователя")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Получение профиля пользователя")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal Long userId) {
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", response));
    }

    @PutMapping("/profile")
    @Operation(summary = "Обновление профиля пользователя")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal Long userId) {
        UserProfileResponse response = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @GetMapping("/stats")
    @Operation(summary = "Получение статистики пользователя")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats(@AuthenticationPrincipal Long userId) {
        UserStatsResponse response = userService.getUserStats(userId);
        return ResponseEntity.ok(ApiResponse.success("User stats retrieved successfully", response));
    }

    @PostMapping("/avatar")
    @Operation(summary = "Загрузка аватара пользователя")
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadAvatar(
            @RequestParam String avatarUrl,
            @AuthenticationPrincipal Long userId) {
        UserProfileResponse response = userService.updateAvatar(userId, avatarUrl);
        return ResponseEntity.ok(ApiResponse.success("Avatar updated successfully", response));
    }

    @DeleteMapping("/avatar")
    @Operation(summary = "Удаление аватара пользователя")
    public ResponseEntity<ApiResponse<UserProfileResponse>> removeAvatar(@AuthenticationPrincipal Long userId) {
        UserProfileResponse response = userService.removeAvatar(userId);
        return ResponseEntity.ok(ApiResponse.success("Avatar removed successfully", response));
    }
}
