package com.task.service;

import com.task.config.ApplicationConfigBean;
import com.task.constants.MainConstants;
import com.task.entity.AppConfig;
import com.task.entity.AppConfigParam;
import com.task.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
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

    private String loadKey() {
        AppConfig configDetail = ApplicationConfigBean.configDetailsMap
                .get(MainConstants.JWT_SECRET_KEY);

        Map<String, AppConfigParam> configParams = configDetail.getParamsMap();

        return configParams.get("JWT_SECRET_KEY").getValue();
    }

    public String extractUserEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        byte[] keyBytes = Base64.getDecoder().decode(loadKey());
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        return buildToken(claims, user, jwtExpirationMs);
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
                .signWith(SignatureAlgorithm.HS256, loadKey())
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
