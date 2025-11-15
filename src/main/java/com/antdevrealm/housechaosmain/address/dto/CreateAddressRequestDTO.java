package com.antdevrealm.housechaosmain.address.dto;

public record CreateAddressRequestDTO(
        String country,
        String city,
        int zip,
        String street
) {
}
