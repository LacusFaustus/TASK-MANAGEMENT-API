package com.taskmanagement.unit.security;

import com.taskmanagement.entity.User;
import com.taskmanagement.service.impl.JwtServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtServiceImpl jwtService;
    private User testUser;

    private final String SECRET_KEY = "mySuperSecretKeyThatIsAtLeast32BytesLongForHS256Algorithm1234567890123456";
    private final Long ACCESS_TOKEN_EXPIRATION = 86400000L; // 24 hours
    private final Long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 days

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();

        // Устанавливаем значения через ReflectionTestUtils
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("$2a$10$encodedPasswordHashHere123456789012")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Test
    void generateAccessToken_WithUser_ShouldGenerateValidToken() {
        // When
        String token = jwtService.generateAccessToken(testUser);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = parseToken(token);
        assertEquals(testUser.getEmail(), claims.getSubject());
        assertEquals(testUser.getId(), claims.get("userId", Long.class));
        assertEquals(testUser.getFirstName(), claims.get("firstName", String.class));
        assertEquals(testUser.getLastName(), claims.get("lastName", String.class));
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void generateRefreshToken_ShouldGenerateValidTokenWithRefreshType() {
        // When
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // Then
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());

        Claims claims = parseToken(refreshToken);
        assertEquals(testUser.getEmail(), claims.getSubject());
        assertEquals("refresh", claims.get("type"));
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void generateToken_WithCustomExpirationAndClaims_ShouldGenerateToken() {
        // Given
        long customExpiration = 5000L; // 5 seconds
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customRole", "ADMIN");
        extraClaims.put("department", "Engineering");

        // When
        String token = jwtService.generateToken(testUser, customExpiration, extraClaims);

        // Then
        assertNotNull(token);

        Claims claims = parseToken(token);
        assertEquals(testUser.getEmail(), claims.getSubject());
        assertEquals(testUser.getId(), claims.get("userId", Long.class));
        assertEquals("ADMIN", claims.get("customRole", String.class));
        assertEquals("Engineering", claims.get("department", String.class));

        // Проверяем время expiration
        long expectedExpiration = System.currentTimeMillis() + customExpiration;
        long actualExpiration = claims.getExpiration().getTime();
        assertTrue(Math.abs(expectedExpiration - actualExpiration) < 1000);
    }

    @Test
    void extractUsername_WithValidToken_ShouldReturnEmail() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertEquals(testUser.getEmail(), username);
    }

    @Test
    void extractClaim_WithValidToken_ShouldReturnClaim() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        String subject = jwtService.extractClaim(token, Claims::getSubject);
        Long userId = jwtService.extractClaim(token, claims -> claims.get("userId", Long.class));

        // Then
        assertEquals(testUser.getEmail(), subject);
        assertEquals(testUser.getId(), userId);
    }

    @Test
    void extractClaim_WithCustomClaim_ShouldReturnCustomValue() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("permissions", "read,write,delete");
        String token = createCustomToken(claims, 10000L);

        // When
        String permissions = jwtService.extractClaim(token,
                c -> c.get("permissions", String.class));

        // Then
        assertEquals("read,write,delete", permissions);
    }

    @Test
    void isTokenValid_WithValidTokenAndUserDetails_ShouldReturnTrue() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        UserDetails userDetails = createUserDetails(testUser.getEmail(), testUser.getPassword());

        // When
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WithValidTokenAndEmail_ShouldReturnTrue() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        boolean isValid = jwtService.isTokenValid(token, testUser.getEmail());

        // Then
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WithInvalidUserDetails_ShouldReturnFalse() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        UserDetails invalidUserDetails = createUserDetails("wrong@example.com", "wrong");

        // When
        boolean isValid = jwtService.isTokenValid(token, invalidUserDetails);

        // Then
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WithInvalidEmail_ShouldReturnFalse() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        boolean isValid = jwtService.isTokenValid(token, "wrong@example.com");

        // Then
        assertFalse(isValid);
    }

    @Test
    void isTokenExpired_WithValidToken_ShouldReturnFalse() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        boolean isExpired = jwtService.isTokenExpired(token);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isTokenExpired_WithExpiredToken_ShouldReturnTrue() {
        // Given - создаем уже истекший токен
        String expiredToken = createExpiredToken();

        // When
        boolean isExpired = jwtService.isTokenExpired(expiredToken);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isTokenValid_WithExpiredToken_ShouldReturnFalse() {
        // Given
        String expiredToken = createExpiredToken();
        UserDetails userDetails = createUserDetails(testUser.getEmail(), testUser.getPassword());

        // When
        boolean isValid = jwtService.isTokenValid(expiredToken, userDetails);

        // Then
        assertFalse(isValid);
    }

    @Test
    void isTokenExpired_WithTokenExpiringNow_ShouldReturnTrue() {
        // Given - токен, который истекает прямо сейчас
        Date now = new Date();
        String expiringNowToken = Jwts.builder()
                .setSubject(testUser.getEmail())
                .setIssuedAt(new Date(now.getTime() - 1000))
                .setExpiration(now)
                .signWith(getSignInKey())
                .compact();

        // When
        boolean isExpired = jwtService.isTokenExpired(expiringNowToken);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void getExpirationTime_ShouldReturnAccessTokenExpiration() {
        // When
        Long expirationTime = jwtService.getExpirationTime();

        // Then
        assertNotNull(expirationTime);
        assertEquals(ACCESS_TOKEN_EXPIRATION, expirationTime);
    }

    @Test
    void isTokenValid_WithNullToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtService.isTokenValid(null, testUser.getEmail());

        // Then
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WithEmptyToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtService.isTokenValid("", testUser.getEmail());

        // Then
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WithMalformedToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtService.isTokenValid("malformed.token.here", testUser.getEmail());

        // Then
        assertFalse(isValid);
    }

    @Test
    void extractUsername_WithMalformedToken_ShouldReturnNull() {
        // When
        String username = jwtService.extractUsername("malformed.token.here");

        // Then
        assertNull(username);
    }

    @Test
    void extractClaim_WithMalformedToken_ShouldReturnNull() {
        // When
        String claim = jwtService.extractClaim("malformed.token.here", Claims::getSubject);

        // Then
        assertNull(claim);
    }

    // Вспомогательные методы

    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private UserDetails createUserDetails(String username, String password) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(password)
                .authorities(Collections.emptyList())
                .build();
    }

    private String createExpiredToken() {
        Date pastDate = new Date(System.currentTimeMillis() - 10000); // 10 секунд назад

        return Jwts.builder()
                .setSubject(testUser.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis() - 20000)) // 20 секунд назад
                .setExpiration(pastDate) // истек 10 секунд назад
                .signWith(getSignInKey())
                .compact();
    }

    private String createCustomToken(Map<String, Object> claims, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(testUser.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
