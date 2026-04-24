package com.antdevrealm.housechaosmain.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

@Component
@ConditionalOnProperty(name = "rate-limiting.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final String LOGIN_PATH   = "/api/v1/auth/login";
    private static final String REFRESH_PATH = "/api/v1/auth/refresh";

    private final ProxyManager<byte[]> proxyManager;
    private final RateLimitProperties  properties;
    private final ObjectMapper         objectMapper;

    public RateLimitFilter(ProxyManager<byte[]> proxyManager,
                           RateLimitProperties properties,
                           ObjectMapper objectMapper) {
        this.proxyManager = proxyManager;
        this.properties   = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        RateLimitProperties.Endpoint cfg = resolveEndpointConfig(path);

        if (cfg == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String slug = LOGIN_PATH.equals(path) ? "login" : "refresh";
        String ip   = extractClientIp(request);
        byte[] key  = ("rate:" + slug + ":" + ip).getBytes();

        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(cfg.getCapacity())
                        .refillGreedy(cfg.getCapacity(), Duration.ofSeconds(cfg.getRefillSeconds()))
                        .build())
                .build();

        try {
            var probe = proxyManager.builder().build(key, configSupplier)
                                    .tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                filterChain.doFilter(request, response);
            } else {
                long retryAfter = probe.getNanosToWaitForRefill() / 1_000_000_000L + 1;
                sendRateLimitResponse(response, retryAfter);
            }
        } catch (Exception e) {
            // Fail-open: if Redis is unavailable, allow the request through rather than
            // returning 500. Rate limiting is a defence layer, not a hard gate.
            log.error("Rate limiter unavailable, failing open: {}", e.getMessage());
            filterChain.doFilter(request, response);
        }
    }

    private RateLimitProperties.Endpoint resolveEndpointConfig(String path) {
        if (LOGIN_PATH.equals(path))   return properties.getLogin();
        if (REFRESH_PATH.equals(path)) return properties.getRefresh();
        return null;
    }

    /**
     * IP extraction priority:
     *   1. CF-Connecting-IP — set by Cloudflare for every proxied request
     *   2. X-Real-IP        — set by nginx upstream proxy directive
     *   3. request.getRemoteAddr() — direct connection (local dev)
     *
     * Known limitation: CF-Connecting-IP and X-Real-IP can be spoofed if a request
     * reaches nginx without passing through Cloudflare. The infrastructure-level fix
     * is to configure nginx to accept connections only from Cloudflare's IP ranges.
     */
    String extractClientIp(HttpServletRequest request) {
        String cfIp = request.getHeader("CF-Connecting-IP");
        if (cfIp != null && !cfIp.isBlank()) return cfIp.trim();

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) return xRealIp.trim();

        return request.getRemoteAddr();
    }

    private void sendRateLimitResponse(HttpServletResponse response, long retryAfterSeconds)
            throws IOException {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
        problem.setType(URI.create("about:blank"));
        problem.setTitle("Too Many Requests");
        problem.setDetail("You have exceeded the request limit. Please try again later.");
        problem.setProperty("timestamp", Instant.now().toString());

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.getWriter().write(objectMapper.writeValueAsString(problem));
    }
}
