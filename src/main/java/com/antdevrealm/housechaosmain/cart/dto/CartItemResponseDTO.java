package com.antdevrealm.housechaosmain.cart.dto;

import java.util.UUID;

public record CartItemResponseDTO(UUID id,
                                  UUID productId,
                                  int quantity) {
}
