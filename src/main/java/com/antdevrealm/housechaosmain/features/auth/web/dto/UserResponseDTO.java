package com.antdevrealm.housechaosmain.features.auth.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDTO(UUID id,
                              String email,
                              boolean active,
                              LocalDateTime createdOn,
                              LocalDateTime updatedAt) {

}
