package com.antdevrealm.housechaosmain.features.auth.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccessTokenResponse(@JsonProperty("access_token") String accessToken,
                                  @JsonProperty("token_type") String tokenType,
                                  @JsonProperty("expires_in") long expiresIn) {
}
