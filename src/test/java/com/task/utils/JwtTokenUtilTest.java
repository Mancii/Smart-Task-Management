package com.task.utils;

import com.task.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;
    private final String testSecret = "mySecretKey12345678901234567890123456789012345678901234567890";
    private final long accessTokenValidity = 86400000L; // 24 hours
    private final long refreshTokenValidity = 604800000L; // 7 days

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        ReflectionTestUtils.setField(jwtTokenUtil, "jwtKey", testSecret);
        ReflectionTestUtils.setField(jwtTokenUtil, "jwtTokenValidityInMs", accessTokenValidity);
        ReflectionTestUtils.setField(jwtTokenUtil, "refreshJwtTokenValidityInMs", refreshTokenValidity);
    }

    @Test
    void generateToken_ShouldCreateValidToken_WhenValidInput() {
        // Given
        String username = "testuser";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");
        claims.put("userId", 1L);

        // When
        String token = jwtTokenUtil.generateToken(username, claims);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots

        // Verify token can be parsed
        Claims parsedClaims = jwtTokenUtil.getAllClaimsFromToken(token);
        assertEquals(username, parsedClaims.getSubject());
        assertEquals("USER", parsedClaims.get("role"));
        assertEquals(1, parsedClaims.get("userId"));
    }

    @Test
    void generateRefreshToken_ShouldCreateValidToken_WhenValidInput() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("tokenType", "refresh");

        // When
        String refreshToken = jwtTokenUtil.generateRefreshToken(claims);

        // Then
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertTrue(refreshToken.split("\\.").length == 3);

        // Verify token can be parsed
        Claims parsedClaims = jwtTokenUtil.getAllClaimsFromToken(refreshToken);
        assertEquals("", parsedClaims.getSubject()); // Refresh token has empty subject
        assertEquals(1, parsedClaims.get("userId"));
        assertEquals("refresh", parsedClaims.get("tokenType"));
    }

    @Test
    void doGenerateToken_ShouldCreateTokenWithCorrectExpiration() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("test", "value");
        String subject = "testsubject";
        long customValidity = 3600000L; // 1 hour

        // When
        String token = jwtTokenUtil.doGenerateToken(claims, subject, customValidity);

        // Then
        assertNotNull(token);
        Claims parsedClaims = jwtTokenUtil.getAllClaimsFromToken(token);
        
        assertEquals(subject, parsedClaims.getSubject());
        assertEquals("value", parsedClaims.get("test"));
        
        // Check expiration is approximately correct (within 1 second)
        long expectedExpiration = System.currentTimeMillis() + customValidity;
        long actualExpiration = parsedClaims.getExpiration().getTime();
        assertTrue(Math.abs(actualExpiration - expectedExpiration) < 1000);
    }

    @Test
    void getAllClaimsFromToken_ShouldReturnCorrectClaims_WhenValidToken() {
        // Given
        Map<String, Object> originalClaims = new HashMap<>();
        originalClaims.put("role", "ADMIN");
        originalClaims.put("userId", 123L);
        originalClaims.put("email", "test@example.com");
        
        String token = jwtTokenUtil.generateToken("testuser", originalClaims);

        // When
        Claims claims = jwtTokenUtil.getAllClaimsFromToken(token);

        // Then
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
        assertEquals("ADMIN", claims.get("role"));
        assertEquals(123, claims.get("userId"));
        assertEquals("test@example.com", claims.get("email"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void getAllClaimsFromToken_ShouldThrowException_WhenInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(Exception.class, () -> jwtTokenUtil.getAllClaimsFromToken(invalidToken));
    }

    @Test
    void getTokenPayload_ShouldReturnPayload_WhenValidToken() throws Exception {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("role", "USER");
        String token = jwtTokenUtil.generateToken("testuser", claims);

        // When
        Map<String, Object> payload = jwtTokenUtil.getTokenPayload(token);

        // Then
        assertNotNull(payload);
        assertEquals("testuser", payload.get("sub"));
        assertEquals(1, payload.get("userId"));
        assertEquals("USER", payload.get("role"));
        assertNotNull(payload.get("iat")); // issued at
        assertNotNull(payload.get("exp")); // expiration
    }

    @Test
    void getTokenPayload_ShouldThrowBusinessException_WhenTokenIsNull() {
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> jwtTokenUtil.getTokenPayload(null));
        
        assertEquals("Access token must not be null or blank", exception.getMessage());
    }

    @Test
    void getTokenPayload_ShouldThrowBusinessException_WhenTokenIsBlank() {
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> jwtTokenUtil.getTokenPayload(""));
        
        assertEquals("Access token must not be null or blank", exception.getMessage());
    }

    @Test
    void getTokenPayload_ShouldThrowBusinessException_WhenTokenHasInvalidFormat() {
        // Given
        String invalidFormatToken = "invalidtoken";

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> jwtTokenUtil.getTokenPayload(invalidFormatToken));
        
        assertEquals("Invalid JWT token format", exception.getMessage());
    }

    @Test
    void getTokenPayload_ShouldThrowBusinessException_WhenTokenHasInvalidBase64() {
        // Given
        String invalidBase64Token = "header.invalid-base64-payload.signature";

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> jwtTokenUtil.getTokenPayload(invalidBase64Token));
        
        assertEquals("Failed to parse token payload", exception.getMessage());
    }

    @Test
    void tokenLifecycle_ShouldWorkCorrectly() {
        // Test complete token lifecycle
        
        // 1. Create token with claims
        Map<String, Object> originalClaims = new HashMap<>();
        originalClaims.put("userId", 42L);
        originalClaims.put("role", "USER");
        originalClaims.put("email", "user@test.com");
        
        String accessToken = jwtTokenUtil.generateToken("testuser", originalClaims);
        String refreshToken = jwtTokenUtil.generateRefreshToken(originalClaims);
        
        // 2. Verify tokens are different
        assertNotEquals(accessToken, refreshToken);
        
        // 3. Parse both tokens and verify claims
        Claims accessClaims = jwtTokenUtil.getAllClaimsFromToken(accessToken);
        Claims refreshClaims = jwtTokenUtil.getAllClaimsFromToken(refreshToken);
        
        // Access token should have subject
        assertEquals("testuser", accessClaims.getSubject());
        assertEquals(42, accessClaims.get("userId"));
        
        // Refresh token should have empty subject
        assertEquals("", refreshClaims.getSubject());
        assertEquals(42, refreshClaims.get("userId"));
        
        // 4. Verify expiration times are different
        long accessExpiration = accessClaims.getExpiration().getTime();
        long refreshExpiration = refreshClaims.getExpiration().getTime();
        assertTrue(refreshExpiration > accessExpiration);
    }

    @Test
    void generateToken_ShouldHandleEmptyClaims() {
        // Given
        Map<String, Object> emptyClaims = new HashMap<>();
        String username = "testuser";

        // When
        String token = jwtTokenUtil.generateToken(username, emptyClaims);

        // Then
        assertNotNull(token);
        Claims claims = jwtTokenUtil.getAllClaimsFromToken(token);
        assertEquals(username, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void generateToken_ShouldHandleSpecialCharactersInClaims() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("specialChars", "!@#$%^&*()");
        claims.put("unicode", "测试用户");
        claims.put("email", "test+user@example.com");
        
        String username = "user@domain.com";

        // When
        String token = jwtTokenUtil.generateToken(username, claims);

        // Then
        assertNotNull(token);
        Claims parsedClaims = jwtTokenUtil.getAllClaimsFromToken(token);
        assertEquals(username, parsedClaims.getSubject());
        assertEquals("!@#$%^&*()", parsedClaims.get("specialChars"));
        assertEquals("测试用户", parsedClaims.get("unicode"));
        assertEquals("test+user@example.com", parsedClaims.get("email"));
    }
}