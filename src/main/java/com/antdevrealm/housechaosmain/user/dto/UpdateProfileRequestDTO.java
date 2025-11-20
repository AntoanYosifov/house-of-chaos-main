package com.antdevrealm.housechaosmain.user.dto;

import com.antdevrealm.housechaosmain.address.dto.AddressRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequestDTO(
        @NotBlank(message = "First name is required")
        String firstName,
        @NotBlank(message = "Last name is required")
        String lastName,
        @Valid
        AddressRequestDTO address) {
}
