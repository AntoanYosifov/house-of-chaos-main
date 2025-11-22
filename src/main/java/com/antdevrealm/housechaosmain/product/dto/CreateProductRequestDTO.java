package com.antdevrealm.housechaosmain.product.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequestDTO(
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

        @NotBlank(message = "Image URL is required")
        @URL(message = "Image URL must be a valid URL")
        String imgUrl,

        @NotNull(message = "Category is required")
        UUID categoryId
) {
}
