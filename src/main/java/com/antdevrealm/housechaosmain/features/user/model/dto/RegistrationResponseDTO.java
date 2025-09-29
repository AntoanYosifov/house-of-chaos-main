package com.antdevrealm.housechaosmain.features.user.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistrationResponseDTO(UUID id,
                                      String email,
                                      boolean active,
                                      LocalDateTime createdOn,
                                      LocalDateTime updatedAt) {

}
