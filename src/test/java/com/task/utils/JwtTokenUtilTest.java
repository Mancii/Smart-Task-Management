package com.task.utils;

import com.task.exception.BusinessException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        // Inject values normally provided by Spring via @Value
        ReflectionTestUtils.setField(jwtTokenUtil, "jwtTokenValidityInMs", 3600_000L);
        ReflectionTestUtils.setField(jwtTokenUtil, "refreshJwtTokenValidityInMs", 7200_000L);
        // Use a key length >= 256 bits for HS256
        ReflectionTestUtils.setField(jwtTokenUtil, "jwtKey", "0123456789ABCDEF0123456789ABCDEF0123456789");
    }

    @Test
    void generateAndParseToken_roundTrip() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");
        String token = jwtTokenUtil.generateToken("john", claims);
        Claims parsed = jwtTokenUtil.getAllClaimsFromToken(token);
        assertEquals("john", parsed.getSubject());
        assertEquals("USER", parsed.get("role"));
        assertNotNull(parsed.getExpiration());
    }

    @Test
    void getTokenPayload_validToken_returnsPayloadMap() throws IOException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        String token = jwtTokenUtil.generateToken("alice", claims);
        Map<String, Object> payload = jwtTokenUtil.getTokenPayload(token);
        assertEquals("alice", payload.get("sub"));
        assertEquals("ADMIN", payload.get("role"));
    }

    @Test
    void getTokenPayload_invalidInputs_throwBusinessException() {
        assertThrows(BusinessException.class, () -> jwtTokenUtil.getTokenPayload(null));
        assertThrows(BusinessException.class, () -> jwtTokenUtil.getTokenPayload(""));
        assertThrows(BusinessException.class, () -> jwtTokenUtil.getTokenPayload("abc.def"));
        assertThrows(BusinessException.class, () -> jwtTokenUtil.getTokenPayload("abc.def.ghi"));
    }
}
