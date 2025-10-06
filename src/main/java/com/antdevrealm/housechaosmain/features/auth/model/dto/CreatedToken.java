package com.antdevrealm.housechaosmain.features.auth.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreatedToken(String rawToken, UUID id, LocalDateTime expiresAt) {
}
