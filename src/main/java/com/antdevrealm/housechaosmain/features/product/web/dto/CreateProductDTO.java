package com.antdevrealm.housechaosmain.features.product.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

public record CreateProductDTO(@NotBlank String name,
                               @NotBlank @Size(min = 10, max = 1000) String description,
                               @Positive @Max(Integer.MAX_VALUE) BigDecimal price,
                               @Positive int quantity,
                               @URL String imgUrl) {
}
