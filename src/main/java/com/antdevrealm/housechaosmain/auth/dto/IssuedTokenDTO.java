package com.antdevrealm.housechaosmain.auth.dto;

import java.time.Instant;

public record IssuedTokenDTO(
        String accessToken,
        String rawRefreshToken,
        Instant refreshExpiresAt,
        long accessTtlSeconds
) {}
