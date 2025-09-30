package com.antdevrealm.housechaosmain.features.user.web.dto;

public record RegistrationRequest(String email,
                                  // TODO: add confirm password field
                                  String password ) {
}
