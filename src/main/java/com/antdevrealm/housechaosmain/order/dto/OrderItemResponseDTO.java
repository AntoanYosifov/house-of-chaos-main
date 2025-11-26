package com.antdevrealm.housechaosmain.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponseDTO(UUID id,
                                   UUID productId,
                                   String productName,
                                   BigDecimal unitPrice,
                                   String imgUrl,
                                   int quantity,
                                   BigDecimal lineTotal) {
}
