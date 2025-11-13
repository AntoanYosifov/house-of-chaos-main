package com.antdevrealm.housechaosmain.user.web.dto;

import com.antdevrealm.housechaosmain.role.model.enums.UserRole;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponseDTO(UUID id,
                              String email,
                              Instant createdOn,
                              Instant updatedAt,
                              List<UserRole> roles) {

}
