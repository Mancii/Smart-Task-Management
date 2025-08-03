package com.task.service;

import com.task.dto.AuthResponse;
import com.task.dto.LogoutResponse;
import com.task.entity.JwtEntity;
import com.task.repo.TokenRepo;
import com.task.utils.JwtTokenUtil;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@AllArgsConstructor
@Service
public class TokenService {

    private final TokenRepo tokenRepo;
    private final JwtTokenUtil jwtTokenUtil;

    private static final Logger logger = LogManager.getLogger(TokenService.class);

    public void saveToken(String token, String refreshToken, Long userId) {

        JwtEntity jwtEntity = tokenRepo.findByUserId(userId);

        if (jwtEntity == null)
            jwtEntity = new JwtEntity();
        jwtEntity.setAccessToken(token);
        jwtEntity.setRefreshToken(refreshToken);
        jwtEntity.setValidId((long) 1);
        jwtEntity.setUserId(userId);
        jwtEntity.setCreatedAt(new Date());
        jwtEntity.setUpdatedAt(new Date());
        tokenRepo.save(jwtEntity);
    }

    public LogoutResponse logout(String token) throws Exception {
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
        JwtEntity jwtEntity = tokenRepo.findByRefreshToken(refreshToken);

        if (jwtEntity.getValidId() != 1) {
            logger.info("Invalid Token");
//            throw new Exception("invalid Token");
        }
        Map<String, Object> claims;
        try {
            claims = jwtTokenUtil.getTokenPayload(jwtEntity.getAccessToken());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String token = jwtTokenUtil.generateToken((String) claims.get("sub"), claims);
        String refToken = jwtTokenUtil.generateRefreshToken(claims);

        saveToken(token, refToken, jwtEntity);

        return new AuthResponse(token, refToken);
    }

    private void saveToken(String token, String refreshToken, JwtEntity jwtEntity) {
        jwtEntity.setAccessToken(token);
        jwtEntity.setRefreshToken(refreshToken);
        jwtEntity.setUpdatedAt(new Date());
        tokenRepo.save(jwtEntity);
    }

}
