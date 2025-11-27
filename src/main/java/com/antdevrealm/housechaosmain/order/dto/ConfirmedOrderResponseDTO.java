package com.antdevrealm.housechaosmain.order.dto;

import com.antdevrealm.housechaosmain.address.dto.AddressResponseDTO;

public record ConfirmedOrderResponseDTO(OrderResponseDTO order, AddressResponseDTO shippingAddress
) {}
