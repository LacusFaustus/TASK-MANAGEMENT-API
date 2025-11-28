package com.taskmanagement.service;

import com.taskmanagement.dto.request.UserUpdateRequest;
import com.taskmanagement.dto.response.UserProfileResponse;
import com.taskmanagement.dto.response.UserStatsResponse;

public interface UserService {
    UserProfileResponse getUserProfile(Long userId);
    UserProfileResponse updateUserProfile(Long userId, UserUpdateRequest request);
    UserStatsResponse getUserStats(Long userId);
    UserProfileResponse updateAvatar(Long userId, String avatarUrl);
    UserProfileResponse removeAvatar(Long userId);
    void deactivateAccount(Long userId);
}
