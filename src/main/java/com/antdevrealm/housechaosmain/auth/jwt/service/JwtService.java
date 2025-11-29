package com.antdevrealm.housechaosmain.auth.jwt.service;

import com.antdevrealm.housechaosmain.auth.model.HOCUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final long ttlSeconds;

    @Autowired
    public JwtService(JwtEncoder jwtEncoder,
                     @Value("${security.jwt.ttl-seconds}") long ttlSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.ttlSeconds = ttlSeconds;
    }

    public String generateToken(HOCUserDetails hocUserDetails) {
        Instant now = Instant.now();

        List<String> authorities = hocUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(this.ttlSeconds, ChronoUnit.SECONDS))
                .subject(hocUserDetails.getUsername())
                .claim("authorities", authorities)
                .claim("uid", hocUserDetails.getUserId().toString())
                .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,claims)).getTokenValue();
    }

    public long ttlSeconds() {
        return this.ttlSeconds;
    }
}
