package com.antdevrealm.housechaosmain.infrastructure.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.time.Duration;

@Configuration
public class RefreshTokenConfig {
    @Value("${security.refresh.token.ttl-days:14}")
    private int ttlDays;

    @Bean("refreshTokenTtl")
    public Duration refreshTtl() {
        return Duration.ofDays(ttlDays);
    }

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
