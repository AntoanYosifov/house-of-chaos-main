package com.antdevrealm.housechaosmain.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequestDTO(
        @NotBlank(message = "Description is required")
        @Size(min = 10, max = 1000, message = "Description must be between {min} and {max} characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01",
                message = "Price must be at least {value}")
        BigDecimal price) {
}
