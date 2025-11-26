package com.antdevrealm.housechaosmain.util;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public final class PrincipalUUIDExtractor {
    public static UUID extract(Jwt principal) {
        String uid = principal.getClaimAsString("uid");
        return UUID.fromString(uid);
    }
}
