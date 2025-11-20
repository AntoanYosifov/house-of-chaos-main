package com.antdevrealm.housechaosmain.address.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressRequestDTO(
        @NotBlank(message = "Country is required")
        String country,

        @NotBlank(message = "City is required")
        String city,

        @NotNull(message = "Zip is required")
        @Min(value = 1, message = "Zip must be positive")
        Integer zip,

        @NotBlank(message = "Street is required")
        String street) {
}
