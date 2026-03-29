package com.antdevrealm.housechaosmain.auth.dto.token;

import java.time.Instant;

public record IssuedTokenDTO(
        String accessToken,
        String rawRefreshToken,
        Instant refreshExpiresAt,
        long accessTtlSeconds
) {}
