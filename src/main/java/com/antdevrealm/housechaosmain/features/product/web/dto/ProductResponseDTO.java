package com.antdevrealm.housechaosmain.features.product.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponseDTO(UUID id,
                                 String name,
                                 String description,
                                 BigDecimal price,
                                 int quantity,
                                 @JsonProperty("img_url") String imgUrl) {
}

