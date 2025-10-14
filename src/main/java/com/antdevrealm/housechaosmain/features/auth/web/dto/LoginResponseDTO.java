package com.antdevrealm.housechaosmain.features.auth.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponseDTO(
        @JsonProperty("access_token") AccessTokenResponseDTO accessTokenResponseDTO,
        @JsonProperty("user") UserResponseDTO userResponseDTO
) {}
