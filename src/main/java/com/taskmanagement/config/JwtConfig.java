package com.taskmanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;
    private Long accessTokenExpiration;
    private Long refreshTokenExpiration;

    // геттеры и сеттеры
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public Long getAccessTokenExpiration() { return accessTokenExpiration; }
    public void setAccessTokenExpiration(Long accessTokenExpiration) { this.accessTokenExpiration = accessTokenExpiration; }
    public Long getRefreshTokenExpiration() { return refreshTokenExpiration; }
    public void setRefreshTokenExpiration(Long refreshTokenExpiration) { this.refreshTokenExpiration = refreshTokenExpiration; }
}
