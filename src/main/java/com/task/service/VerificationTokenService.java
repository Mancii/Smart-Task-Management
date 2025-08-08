package com.task.service;

import com.task.entity.User;
import com.task.entity.VerificationToken;
import com.task.exception.InvalidTokenException;
import com.task.repo.UserRepository;
import com.task.repo.VerificationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class VerificationTokenService {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public VerificationToken createVerificationToken(User user) {
        // Delete any existing tokens for this user
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        return tokenRepository.save(verificationToken);
    }

    public VerificationToken getVerificationToken(String token) {
        return tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));
    }

    @Transactional
    public void verifyEmailToken(String token) {
        // Find the verification token
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification link."));

        // Check if token is expired
        if (verificationToken.isExpired()) {
            tokenRepository.delete(verificationToken);
            throw new InvalidTokenException("Verification link has expired. Please register again.");
        }

        // Check if token is already used
        if (verificationToken.isUsed()) {
            throw new InvalidTokenException("This verification link has already been used.");
        }

        // Get and enable the user
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        // Mark token as used and delete it
        verificationToken.setUsed(true);
        tokenRepository.delete(verificationToken);
    }

    @Transactional
    public void deleteToken(VerificationToken token) {
        tokenRepository.delete(token);
    }
    
    public Optional<VerificationToken> findByToken(String token) {
        return tokenRepository.findByToken(token);
    }
}
