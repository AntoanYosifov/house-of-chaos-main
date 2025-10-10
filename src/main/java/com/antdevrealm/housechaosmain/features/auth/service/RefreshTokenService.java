package com.antdevrealm.housechaosmain.features.auth.service;

import com.antdevrealm.housechaosmain.features.auth.exception.RefreshTokenInvalidException;
import com.antdevrealm.housechaosmain.features.auth.model.dto.CreatedRefreshToken;
import com.antdevrealm.housechaosmain.features.auth.model.dto.RotationRefreshTokenResult;
import com.antdevrealm.housechaosmain.features.auth.model.entity.RefreshTokenEntity;
import com.antdevrealm.housechaosmain.features.auth.repository.RefreshTokenRepository;
import com.antdevrealm.housechaosmain.features.auth.util.TokenHasher;
import com.antdevrealm.housechaosmain.features.user.model.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

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

    public CreatedRefreshToken create(UserEntity user) {
        String raw = generateRawToken();
        String tokenHash = tokenHasher.hash(raw);

        LocalDateTime expiresAt = LocalDateTime.now().plus(refreshTokenTtl);

        RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        RefreshTokenEntity saved = tokenRepository.save(tokenEntity);

        return new CreatedRefreshToken(raw, saved.getId(), saved.getExpiresAt());

    }

    @Transactional
    public RotationRefreshTokenResult rotateInPlace(String rawToken) {
        if(rawToken == null || rawToken.isBlank()) {
            throw new RefreshTokenInvalidException();
        }

        String currentHash = tokenHasher.hash(rawToken);
        RefreshTokenEntity refreshTokenEntity = tokenRepository.findByTokenHash(currentHash)
                .orElseThrow(RefreshTokenInvalidException::new);

        if(!refreshTokenEntity.isActive()) {
            throw new RefreshTokenInvalidException();
        }

        String newRaw = generateRawToken();
        String newHash = tokenHasher.hash(newRaw);

        refreshTokenEntity.setTokenHash(newHash);
        RefreshTokenEntity rotatedToken = tokenRepository.save(refreshTokenEntity);
        UserEntity user = rotatedToken.getUser();

        return new RotationRefreshTokenResult(user.getEmail(), newRaw, rotatedToken.getExpiresAt());
    }

    private String generateRawToken() {
        byte[] buf = new byte[32];
        secureRandom.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
