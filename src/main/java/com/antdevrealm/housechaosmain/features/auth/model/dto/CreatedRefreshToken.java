package com.antdevrealm.housechaosmain.features.auth.model.dto;

import java.time.Instant;
import java.util.UUID;

public record CreatedRefreshToken(String rawToken, UUID id, Instant expiresAt) {
}
