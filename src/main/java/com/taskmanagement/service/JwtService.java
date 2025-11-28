package com.taskmanagement.service;

import com.taskmanagement.entity.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.function.Function;

public interface JwtService {
    String extractUsername(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    String generateToken(User user, long expiration, Map<String, Object> extraClaims);
    boolean isTokenValid(String token, UserDetails userDetails);
    boolean isTokenValid(String token, String email);
    boolean isTokenExpired(String token);
    Long getExpirationTime();
}
