package com.antdevrealm.housechaosmain.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequestDTO(
        @NotBlank(message = "Name is required")
        String name
) {}
