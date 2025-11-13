package com.antdevrealm.housechaosmain.auth.refreshtoken.model.dto;

import java.time.Instant;

public record RotationRefreshTokenResultDTO(String userEmail, String newRaw, Instant expiresAt) {}
