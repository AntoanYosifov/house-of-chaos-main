package com.antdevrealm.housechaosmain.cart.dto;

import java.util.List;
import java.util.UUID;

public record CartResponseDTO(UUID id,
                              UUID ownerId,
                              List<CartItemResponseDTO> items) {
}
