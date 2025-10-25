package com.task.service;

import com.task.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.access.token.validity}")
    private long jwtExpirationMs;
    @Value("${jwt.refresh.token.validity}")
    private long refreshExpirationMs;
    @Value("${jwt.secret}")
    private String jwtKey;

//    private SecretKey getSigningKey() {
//        AppConfig configDetail = ApplicationConfigBean.configDetailsMap
//                .get(MainConstants.JWT_SECRET_KEY);
//
//        Map<String, AppConfigParam> configParams = configDetail.getParamsMap();
//        String secret = configParams.get("JWT_SECRET_KEY").getValue();
//        return Keys.hmacShaKeyFor(secret.getBytes());
//    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtKey.getBytes());
    }

    public String extractUserEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(User user) {
        return buildToken(new HashMap<>(), user, jwtExpirationMs);
    }

    public String generateRefreshToken(User user) {
        return buildToken(new HashMap<>(), user, refreshExpirationMs);
    }

    private String buildToken(Map<String, Object> claims, User user, long expiration) {

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .claim("authorities", user.getAuthorities())
                .signWith(getSigningKey())
                .compact();
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean isTokenValid(String token, User user) {
        final String email = extractUserEmail(token);
        return (email.equals(user.getEmail()) && !isTokenExpired(token));
    }
}
