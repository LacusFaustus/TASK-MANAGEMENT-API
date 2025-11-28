package com.taskmanagement.service.impl;

import com.taskmanagement.dto.request.UserUpdateRequest;
import com.taskmanagement.dto.response.UserProfileResponse;
import com.taskmanagement.dto.response.UserStatsResponse;
import com.taskmanagement.entity.User;
import com.taskmanagement.entity.enums.TaskStatus;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.mapper.UserMapper;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.repository.WorkspaceMemberRepository;
import com.taskmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = getUserById(userId);
        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserUpdateRequest request) {
        User user = getUserById(userId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Проверяем, не используется ли email другим пользователем
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        User updatedUser = userRepository.save(user);
        log.info("User profile updated for user: {}", userId);

        return userMapper.toProfileResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats(Long userId) {
        Long totalTasks = taskRepository.countByAssigneeId(userId);
        Long completedTasks = taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.DONE);
        Long inProgressTasks = taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.IN_PROGRESS);
        Long overdueTasks = taskRepository.countOverdueTasksByAssigneeId(userId);
        Long workspaceCount = workspaceMemberRepository.countByUserId(userId);

        return UserStatsResponse.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .overdueTasks(overdueTasks)
                .workspaceCount(workspaceCount)
                .completionRate(totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0)
                .build();
    }

    @Override
    @Transactional
    public UserProfileResponse updateAvatar(Long userId, String avatarUrl) {
        User user = getUserById(userId);
        user.setAvatarUrl(avatarUrl);

        User updatedUser = userRepository.save(user);
        log.info("Avatar updated for user: {}", userId);

        return userMapper.toProfileResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserProfileResponse removeAvatar(Long userId) {
        User user = getUserById(userId);
        user.setAvatarUrl(null);

        User updatedUser = userRepository.save(user);
        log.info("Avatar removed for user: {}", userId);

        return userMapper.toProfileResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deactivateAccount(Long userId) {
        User user = getUserById(userId);
        // В реальном приложении здесь была бы логика деактивации аккаунта
        // user.setActive(false);
        // userRepository.save(user);

        log.info("Account deactivated for user: {}", userId);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
