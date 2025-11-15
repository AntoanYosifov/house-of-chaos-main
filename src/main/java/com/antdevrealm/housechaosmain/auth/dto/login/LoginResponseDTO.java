package com.antdevrealm.housechaosmain.auth.dto.login;

import com.antdevrealm.housechaosmain.auth.dto.accesstoken.AccessTokenResponseDTO;
import com.antdevrealm.housechaosmain.user.dto.UserResponseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponseDTO(
        @JsonProperty("access_token") AccessTokenResponseDTO accessTokenResponseDTO,
        @JsonProperty("user") UserResponseDTO userResponseDTO
) {}
