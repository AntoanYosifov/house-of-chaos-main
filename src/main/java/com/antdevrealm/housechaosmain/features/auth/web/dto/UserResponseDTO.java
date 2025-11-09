package com.antdevrealm.housechaosmain.features.auth.web.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDTO(UUID id,
                              String email,
                              Instant createdOn,
                              Instant updatedAt) {

}
