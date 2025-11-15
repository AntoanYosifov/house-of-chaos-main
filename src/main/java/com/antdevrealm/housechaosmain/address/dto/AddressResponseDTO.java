package com.antdevrealm.housechaosmain.address.dto;

import java.time.Instant;
import java.util.UUID;

public record AddressResponseDTO(UUID id,
                                 String country,
                                 String city,
                                 int zip,
                                 String street,
                                 Instant createdOn,
                                 Instant updatedAt
                                 ) {}
