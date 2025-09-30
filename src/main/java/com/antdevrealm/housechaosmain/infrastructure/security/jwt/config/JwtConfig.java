package com.antdevrealm.housechaosmain.infrastructure.security.jwt.config;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Base64;

@Configuration
public class JwtConfig {
    @Value("${security.jwt.secret-base64}")
    private String secretB64;
    // Short expiration for testing purposes
    @Value("${security.jwt.ttl-seconds:60}")
    private long ttlSeconds;

    @Bean
    public SecretKey jwtSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretB64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Bean
    public JwtParser jwtParser(SecretKey secretKey) {
        return Jwts.parser().verifyWith(secretKey).build();
    }

    @Bean
    public Duration jwtTtl() {
        return Duration.ofSeconds(ttlSeconds);
    }
}
