package com.antdevrealm.housechaosmain.order.dto;

import com.antdevrealm.housechaosmain.address.dto.AddressResponseDTO;
import com.antdevrealm.housechaosmain.order.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponseDTO(UUID id,
                               UUID ownerId,
                               OrderStatus status,
                               Instant createdOn,
                               Instant updatedAt,
                               BigDecimal total,
                               AddressResponseDTO shippingAddress,
                               List<OrderItemResponseDTO> items) {
}
