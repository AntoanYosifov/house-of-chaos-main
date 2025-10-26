package com.antdevrealm.housechaosmain.features.product.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

public record CreateProductDTO(@NotBlank String name,
                               @NotBlank @Size(min = 10, max = 1000) String description,
                               @Positive @Max(Integer.MAX_VALUE) BigDecimal price,
                               @Positive int quantity,
                               @JsonProperty("img_url") @URL String imgUrl) {
}
