package com.antdevrealm.housechaosmain.security.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RateLimitFilterITest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("rate-limiting.enabled", () -> "true");
        // Re-enable Redis auto-config (overrides the exclude in test application.properties)
        registry.add("spring.autoconfigure.exclude", () -> "");
    }

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void flushRedis() throws Exception {
        // Flush Redis between tests so bucket state does not bleed across test methods.
        // The container is shared for the class so we must clear manually.
        redis.execInContainer("redis-cli", "FLUSHALL");
    }

    @Test
    void givenLoginEndpoint_whenRequestsExceedLimit_thenSixthRequestReturns429() throws Exception {
        String body = """
                {"email":"attacker@example.com","password":"wrong"}
                """;

        // First 5 requests should not be rate-limited (will be 400 or 401)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .with(request -> { request.setRemoteAddr("10.10.10.10"); return request; }))
                    .andExpect(result ->
                        assertThat(result.getResponse().getStatus()).isNotEqualTo(429));
        }

        // 6th request must hit the rate limit
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(request -> { request.setRemoteAddr("10.10.10.10"); return request; }))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Too Many Requests"));
    }

    @Test
    void givenDifferentIPs_whenEachSendsFiveRequests_thenNeitherIsRateLimited() throws Exception {
        String body = """
                {"email":"user@example.com","password":"wrong"}
                """;

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .with(request -> { request.setRemoteAddr("1.1.1.1"); return request; }))
                    .andExpect(result ->
                        assertThat(result.getResponse().getStatus()).isNotEqualTo(429));

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .with(request -> { request.setRemoteAddr("2.2.2.2"); return request; }))
                    .andExpect(result ->
                        assertThat(result.getResponse().getStatus()).isNotEqualTo(429));
        }
    }
}
