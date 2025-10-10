package com.antdevrealm.housechaosmain.features.auth.model.dto;

import java.time.LocalDateTime;

public record RotationRefreshTokenResult(String userEmail, String newRaw, LocalDateTime expiresAt) {}
