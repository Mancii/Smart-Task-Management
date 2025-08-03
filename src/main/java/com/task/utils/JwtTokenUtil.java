package com.task.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.config.ApplicationConfigBean;
import com.task.constants.MainConstants;
import com.task.entity.AppConfig;
import com.task.entity.AppConfigParam;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.Serializable;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    // JWT_TOKEN_VALIDITY_IN_MS = 5 minute
    @Value("${jwt.access.token.validity}")
    public long JWT_TOKEN_VALIDITY_IN_MS;

    // REFRESH_JWT_TOKEN_VALIDITY_IN_MS = 30 minute
    @Value("${jwt.refresh.token.validity}")
    public long REFRESH_JWT_TOKEN_VALIDITY_IN_MS;

    private static final Logger logger = LogManager.getLogger(JwtTokenUtil.class);

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
        ObjectMapper mapper = new ObjectMapper();

        logger.info("------------ Decode JWT ------------");

        String[] splitString = accessToken.split("\\.");
        if (splitString.length < 2) {
            throw new IllegalArgumentException("Invalid JWT token format");
        }

        String base64EncodedBody = splitString[1];

        // Decode the JWT body using Java's Base64 URL decoder
        byte[] decodedBytes = Base64.getUrlDecoder().decode(base64EncodedBody);
        String body = new String(decodedBytes);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        return mapper.readValue(body, Map.class);
    }
//
//    // generate token for user
    public String generateToken(String userName, Map<String, Object> userClaims) {
        Map<String, Object> claims = userClaims;
        return doGenerateToken(claims, userName, JWT_TOKEN_VALIDITY_IN_MS);
    }
//
    public String generateRefreshToken(Map<String, Object> userClaims) {
        Map<String, Object> claims = userClaims;
        return doGenerateToken(claims, "", REFRESH_JWT_TOKEN_VALIDITY_IN_MS);
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
