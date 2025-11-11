package com.antdevrealm.housechaosmain.features.auth.web.dto;

import com.antdevrealm.housechaosmain.features.role.model.enums.UserRole;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponseDTO(UUID id,
                              String email,
                              Instant createdOn,
                              Instant updatedAt,
                              List<UserRole> roles) {

}
