package com.antdevrealm.housechaosmain.user.dto;

import com.antdevrealm.housechaosmain.address.dto.AddressRequestDTO;

//TODO: add validation annotations
public record UpdateProfileRequestDTO(String firstName,
                                      String lastName,
                                      AddressRequestDTO address) {
}
