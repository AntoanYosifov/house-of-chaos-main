package com.antdevrealm.housechaosmain.features.auth.model.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record RotationRefreshTokenResult(String userEmail, String newRaw, Instant expiresAt) {}
