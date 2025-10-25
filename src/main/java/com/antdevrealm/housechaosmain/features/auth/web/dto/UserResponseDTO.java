package com.antdevrealm.housechaosmain.features.auth.web.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDTO(UUID id,
                              String email,
                              boolean active,
                              Instant createdOn,
                              Instant updatedAt) {

}
