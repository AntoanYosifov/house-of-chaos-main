package com.antdevrealm.housechaosmain.features.auth.service;

import com.antdevrealm.housechaosmain.features.auth.repository.RefreshTokenRepository;
import com.antdevrealm.housechaosmain.features.auth.util.TokenHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

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
}
