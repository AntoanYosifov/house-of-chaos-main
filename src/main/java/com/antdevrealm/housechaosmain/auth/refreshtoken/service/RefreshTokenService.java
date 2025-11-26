package com.antdevrealm.housechaosmain.auth.refreshtoken.service;

import com.antdevrealm.housechaosmain.auth.refreshtoken.dto.CreatedRefreshTokenDTO;
import com.antdevrealm.housechaosmain.auth.refreshtoken.model.RefreshTokenEntity;
import com.antdevrealm.housechaosmain.auth.refreshtoken.repository.RefreshTokenRepository;
import com.antdevrealm.housechaosmain.auth.refreshtoken.exception.RefreshTokenInvalidException;
import com.antdevrealm.housechaosmain.auth.refreshtoken.dto.RotationRefreshTokenResultDTO;
import com.antdevrealm.housechaosmain.auth.refreshtoken.util.TokenHasher;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository tokenRepository;
    private final TokenHasher tokenHasher;
    private final SecureRandom secureRandom;
    private final Duration refreshTokenTtl;

    @Autowired
    public RefreshTokenService(RefreshTokenRepository tokenRepository,
                               TokenHasher tokenHasher,
                               SecureRandom secureRandom,
                               @Qualifier("refreshTokenTtl") Duration refreshTokenTtl) {
        this.tokenRepository = tokenRepository;
        this.tokenHasher = tokenHasher;
        this.secureRandom = secureRandom;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    @Transactional
    public CreatedRefreshTokenDTO create(UserEntity user) {
        deleteByUserId(user.getId());
        String raw = generateRawToken();
        String tokenHash = tokenHasher.hash(raw);

        Instant expiresAt = Instant.now().plus(refreshTokenTtl);

        RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        RefreshTokenEntity saved = tokenRepository.save(tokenEntity);

        return new CreatedRefreshTokenDTO(raw, saved.getId(), saved.getExpiresAt());
    }

    public void deleteByUserId(UUID userId) {
        tokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteByTokenHash(String rawToken) {
        String hash = tokenHasher.hash(rawToken);
        tokenRepository.deleteByTokenHash(hash);
    }

    @Transactional
    public RotationRefreshTokenResultDTO rotateInPlace(String rawToken) {
        String currentHash = tokenHasher.hash(rawToken);
        RefreshTokenEntity refreshTokenEntity = tokenRepository.findByTokenHash(currentHash)
                .orElseThrow(() -> new RefreshTokenInvalidException("Refresh token not found"));

        if (!refreshTokenEntity.isActive()) {
            throw new RefreshTokenInvalidException("Refresh token is revoked or expired");
        }

        String newRaw = generateRawToken();
        String newHash = tokenHasher.hash(newRaw);

        refreshTokenEntity.setTokenHash(newHash);
        RefreshTokenEntity rotatedToken = tokenRepository.save(refreshTokenEntity);
        UserEntity user = rotatedToken.getUser();

        return new RotationRefreshTokenResultDTO(user.getEmail(), newRaw, rotatedToken.getExpiresAt());
    }

    private String generateRawToken() {
        byte[] buf = new byte[32];
        secureRandom.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
