package com.antdevrealm.housechaosmain.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductForm(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Description is required")
        @Size(min = 10, max = 1000, message = "Description must be between {min} and {max} characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01",
                message = "Price must be at least {value}")
        BigDecimal price,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least {value}")
        Integer quantity,

        @NotNull(message = "Category is required")
        UUID categoryId
) {
}
