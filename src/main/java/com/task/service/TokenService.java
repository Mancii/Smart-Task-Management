package com.task.service;

import com.task.constants.MainConstants;
import com.task.dto.AuthResponse;
import com.task.dto.LogoutResponse;
import com.task.entity.JwtEntity;
import com.task.exception.BusinessException;
import com.task.repo.TokenRepo;
import com.task.utils.JwtTokenUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@AllArgsConstructor
@Service
@Slf4j
public class TokenService {

    private final TokenRepo tokenRepo;
    private final JwtTokenUtil jwtTokenUtil;

    public void saveToken(String token, String refreshToken, Long userId) {

        log.debug("Saving tokens for user ID: {}", userId);

        JwtEntity jwtEntity = tokenRepo.findByUserId(userId).orElseGet(() -> createNewJwtEntity(userId));

        updateTokenEntity(jwtEntity, token, refreshToken);

        JwtEntity savedEntity = tokenRepo.save(jwtEntity);

        log.debug("Successfully saved tokens for user ID: {}", savedEntity.getUserId());
    }

    private JwtEntity createNewJwtEntity(Long userId) {
        log.debug("Creating new JWT entity for user ID: {}", userId);
        JwtEntity entity = new JwtEntity();
        entity.setUserId(userId);
        entity.setValidId(MainConstants.ACCOUNT_ACTIVE);
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    private void updateTokenEntity(JwtEntity entity, String token, String refreshToken) {
        entity.setAccessToken(token);
        entity.setRefreshToken(refreshToken);
        entity.setUpdatedAt(Instant.now());

        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
    }

    public LogoutResponse logout(String token) {
        Integer x = tokenRepo.invalidateToken(token, new Date());

        if (x == null || x == 0)
            throw new IllegalArgumentException("Error has occurred");

        return new LogoutResponse("logout successfully");
    }

    public LogoutResponse kill(long tokenId) {
        Integer x = tokenRepo.invalidateTokenById(tokenId, new Date());

        if (x == null || x == 0)
            throw new IllegalArgumentException("Error has occurred");

        return new LogoutResponse("logout successfully");
    }

    public AuthResponse getUserNameFromTokenUsingRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException("Refresh token must not be null or blank");
        }

        JwtEntity jwtEntity = tokenRepo.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException("Refresh token not found"));

        if (jwtEntity.getValidId() != 1)
            throw new BusinessException("invalid Token");


        Map<String, Object> claims;
        try {
            claims = jwtTokenUtil.getTokenPayload(jwtEntity.getAccessToken());
        } catch (IOException e) {
            throw new BusinessException(e);
        }

        String token = jwtTokenUtil.generateToken((String) claims.get("sub"), claims);
        String refToken = jwtTokenUtil.generateRefreshToken(claims);

        saveToken(token, refToken, jwtEntity);

        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(refToken)
                .build();
    }

    private void saveToken(String token, String refreshToken, JwtEntity jwtEntity) {
        jwtEntity.setAccessToken(token);
        jwtEntity.setRefreshToken(refreshToken);
        jwtEntity.setUpdatedAt(Instant.now());
        tokenRepo.save(jwtEntity);
    }

}
