package com.antdevrealm.housechaosmain.auth.web.dto;

import com.antdevrealm.housechaosmain.user.web.dto.UserResponseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponseDTO(
        @JsonProperty("access_token") AccessTokenResponseDTO accessTokenResponseDTO,
        @JsonProperty("user") UserResponseDTO userResponseDTO
) {}
