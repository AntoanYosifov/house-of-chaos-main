package com.antdevrealm.housechaosmain.address.dto;

public record AddressRequestDTO(
        String country,
        String city,
        int zip,
        String street
) {
}
