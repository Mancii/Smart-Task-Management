package com.task.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.config.ApplicationConfigBean;
import com.task.constants.MainConstants;
import com.task.entity.AppConfig;
import com.task.entity.AppConfigParam;
import com.task.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    @Value("${jwt.access.token.validity}")
    public long jwtTokenValidityInMs;

    @Value("${jwt.refresh.token.validity}")
    public long refreshJwtTokenValidityInMs;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

    private String loadKey() {
        AppConfig configDetail = ApplicationConfigBean.configDetailsMap
                .get(MainConstants.JWT_SECRET_KEY);

        Map<String, AppConfigParam> configParams = configDetail.getParamsMap();

        return configParams.get("JWT_SECRET_KEY").getValue();
    }

    // for retrieving any information from token we will need the secret key
    public Claims getAllClaimsFromToken(String token) {
        // Load your secret key (make sure it's at least 256 bits for HS256)
        byte[] keyBytes = Base64.getDecoder().decode(loadKey()); // use plain .getBytes() if not base64
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Map<String, Object> getTokenPayload(String accessToken) throws IOException {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BusinessException("Access token must not be null or blank");
        }

        String[] splitString = accessToken.split("\\.");
        if (splitString.length < 2) {
            throw new BusinessException("Invalid JWT token format");
        }

        String base64EncodedBody = splitString[1];

        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(base64EncodedBody);
            String body = new String(decodedBytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(body, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse token payload", e);
            throw new BusinessException("Failed to parse token payload", e);
        }

    }

//    // generate token for user
    public String generateToken(String userName, Map<String, Object> userClaims) {
        Map<String, Object> claims = userClaims;
        return doGenerateToken(claims, userName, jwtTokenValidityInMs);
    }

    public String generateRefreshToken(Map<String, Object> userClaims) {
        Map<String, Object> claims = userClaims;
        return doGenerateToken(claims, "", refreshJwtTokenValidityInMs);
    }
//
//    // while creating the token -
//    // 1. Define claims of the token, like Issuer, Expiration, Subject, and the ID
//    // 2. Sign the JWT using the HS256 algorithm and secret key.
//    // 3. According to JWS Compact
//    // Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
//    // compaction of the JWT to a URL-safe string
    public String doGenerateToken(Map<String, Object> claims, String subject, Long Time) {

        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + Time))
                .signWith(SignatureAlgorithm.HS256, loadKey().getBytes())
                .compact();
    }
////
////    public boolean validateJwtToken(String authToken) throws Exception {
////        try {
////            Jwts.parser().setSigningKey(loadKey().getBytes()).parseClaimsJws(authToken);
////            return true;
////        } catch (SignatureException e) {
////            throw new SignatureException("Invalid Token signature");
////        } catch (MalformedJwtException e) {
////            throw new SignatureException("Invalid Token token");
////        } catch (ExpiredJwtException e) {
////            throw new SignatureException("Token is expired");
////        } catch (UnsupportedJwtException e) {
////            throw new SignatureException("Token is unsupported");
////        } catch (IllegalArgumentException e) {
////            throw new SignatureException("Token claims string is empty");
////        } catch (Exception e) {
////            throw new Exception("Unknown Token Validation Exception");
////        }
////    }
//
}
