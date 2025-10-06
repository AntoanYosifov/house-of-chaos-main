package com.antdevrealm.housechaosmain.infrastructure.security.jwt.service;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey secretKey;
    private final JwtParser parser;
    private final Duration ttl;

    public JwtService(SecretKey secretKey, JwtParser parser, @Qualifier("jwtTtl") Duration ttl) {
        this.secretKey = secretKey;
        this.parser = parser;
        this.ttl = ttl;
    }

    public String generateToken(String subject) {
        var now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String extractSubjectFromToken(String token) {
        return parser.parseSignedClaims(token).getPayload().getSubject();
    }

    public long ttlSeconds() {
        return this.ttl.toSeconds();
    }
}
