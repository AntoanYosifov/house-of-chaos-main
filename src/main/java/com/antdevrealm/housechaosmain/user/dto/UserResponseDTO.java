package com.antdevrealm.housechaosmain.user.dto;

import com.antdevrealm.housechaosmain.address.dto.AddressResponseDTO;
import com.antdevrealm.housechaosmain.role.model.enums.UserRole;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponseDTO(UUID id,
                              String email,
                              String firstName,
                              String lastName,
                              AddressResponseDTO address,
                              Instant createdOn,
                              Instant updatedAt,
                              List<UserRole> roles) {

}
