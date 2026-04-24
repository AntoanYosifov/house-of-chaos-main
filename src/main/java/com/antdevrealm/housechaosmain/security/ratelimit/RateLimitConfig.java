package com.antdevrealm.housechaosmain.security.ratelimit;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnProperty(name = "rate-limiting.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitConfig {

    @Bean
    public ProxyManager<byte[]> rateLimitProxyManager(LettuceConnectionFactory factory) {
        RedisClient nativeClient = (RedisClient) factory.getRequiredNativeClient();
        return Bucket4jLettuce.casBasedBuilder(nativeClient).build();
    }
}
