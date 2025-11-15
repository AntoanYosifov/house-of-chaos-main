package com.antdevrealm.housechaosmain.auth.dto.accesstoken;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccessTokenResponseDTO(@JsonProperty("access_token") String accessToken,
                                     @JsonProperty("token_type") String tokenType,
                                     @JsonProperty("expires_in") long expiresIn) {
}
