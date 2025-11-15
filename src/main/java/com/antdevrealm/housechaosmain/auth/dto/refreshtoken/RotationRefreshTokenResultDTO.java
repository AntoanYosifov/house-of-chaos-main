package com.antdevrealm.housechaosmain.auth.dto.refreshtoken;

import java.time.Instant;

public record RotationRefreshTokenResultDTO(String userEmail, String newRaw, Instant expiresAt) {}
