package com.taskmanagement.service.impl;

import com.taskmanagement.dto.request.LoginRequest;
import com.taskmanagement.dto.request.RegisterRequest;
import com.taskmanagement.dto.response.AuthResponse;
import com.taskmanagement.dto.response.UserProfileResponse;
import com.taskmanagement.entity.User;
import com.taskmanagement.exception.BusinessException;
import com.taskmanagement.mapper.UserMapper;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.service.AuthService;
import com.taskmanagement.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered with email: {}", request.getEmail());

        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);
        UserProfileResponse userProfile = userMapper.toProfileResponse(savedUser);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getExpirationTime())
                .user(userProfile)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BusinessException("User not found"));

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            UserProfileResponse userProfile = userMapper.toProfileResponse(user);

            log.info("User logged in with email: {}", request.getEmail());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtService.getExpirationTime())
                    .user(userProfile)
                    .build();

        } catch (BadCredentialsException e) {
            throw new BusinessException("Invalid email or password");
        }
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        try {
            String email = jwtService.extractUsername(refreshToken);

            if (email == null) {
                throw new BusinessException("Invalid refresh token");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException("User not found"));

            if (!jwtService.isTokenValid(refreshToken, user.getEmail())) {
                throw new BusinessException("Invalid refresh token");
            }

            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);
            UserProfileResponse userProfile = userMapper.toProfileResponse(user);

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(jwtService.getExpirationTime())
                    .user(userProfile)
                    .build();

        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new BusinessException("Token refresh failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found with email: " + email));

        // Generate reset token (15 minutes expiration)
        String resetToken = jwtService.generateToken(user, 900000L, null);
        // emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

        log.info("Password reset initiated for email: {}", email);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        try {
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException("Invalid reset token"));

            if (!jwtService.isTokenValid(token, user.getEmail())) {
                throw new BusinessException("Invalid or expired reset token");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            log.info("Password reset successfully for email: {}", email);

        } catch (Exception e) {
            log.error("Password reset failed", e);
            throw new BusinessException("Password reset failed: " + e.getMessage());
        }
    }
}
