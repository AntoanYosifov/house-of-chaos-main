package com.antdevrealm.housechaosmain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequestDTO(
        @NotNull(message = "Items are required")
        @Size(min = 1, message = "Order must contain at least one item")
        @Valid
        List<CreateOrderItemRequestDTO> items
) {
}
