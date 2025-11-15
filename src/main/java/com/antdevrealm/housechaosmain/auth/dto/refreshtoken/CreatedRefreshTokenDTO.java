package com.antdevrealm.housechaosmain.auth.dto.refreshtoken;

import java.time.Instant;
import java.util.UUID;

public record CreatedRefreshTokenDTO(String rawToken, UUID id, Instant expiresAt) {
}
