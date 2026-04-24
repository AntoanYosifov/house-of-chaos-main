package com.antdevrealm.housechaosmain.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterUTest {

    @Mock ProxyManager<byte[]> proxyManager;
    @Mock FilterChain filterChain;

    RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter(proxyManager, new RateLimitProperties(), new ObjectMapper());
    }

    // ---- helper: wire proxyManager to return a bucket with the given probe ----

    @SuppressWarnings("unchecked")
    private void givenBucket(ConsumptionProbe probe) {
        BucketProxy bucket = mock(BucketProxy.class);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        RemoteBucketBuilder<byte[]> builder = mock(RemoteBucketBuilder.class);
        when(builder.build(any(byte[].class), any(Supplier.class))).thenReturn(bucket);
        when(proxyManager.builder()).thenReturn(builder);
    }

    // ---- tests ----

    @Test
    void givenNonRateLimitedPath_whenFilter_thenPassesThroughWithoutCheckingRedis() throws Exception {
        var req = new MockHttpServletRequest("GET", "/api/v1/products");
        var res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        verify(filterChain).doFilter(req, res);
        verifyNoInteractions(proxyManager);
    }

    @Test
    void givenLoginPathUnderLimit_whenFilter_thenRequestPassesThrough() throws Exception {
        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(true);
        givenBucket(probe);

        var req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req.setRemoteAddr("10.0.0.1");
        var res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        verify(filterChain).doFilter(req, res);
    }

    @Test
    void givenLoginPathOverLimit_whenFilter_thenReturns429WithRetryAfterHeader() throws Exception {
        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(false);
        when(probe.getNanosToWaitForRefill()).thenReturn(30_000_000_000L); // 30 s
        givenBucket(probe);

        var req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req.setRemoteAddr("10.0.0.1");
        var res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        verifyNoInteractions(filterChain);
        assertThat(res.getStatus()).isEqualTo(429);
        assertThat(res.getHeader("Retry-After")).isEqualTo("31");
        assertThat(res.getContentType()).contains("application/problem+json");
    }

    @Test
    void givenRefreshPathOverLimit_whenFilter_thenReturns429() throws Exception {
        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(false);
        when(probe.getNanosToWaitForRefill()).thenReturn(10_000_000_000L); // 10 s
        givenBucket(probe);

        var req = new MockHttpServletRequest("POST", "/api/v1/auth/refresh");
        req.setRemoteAddr("10.0.0.1");
        var res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        assertThat(res.getStatus()).isEqualTo(429);
        assertThat(res.getHeader("Retry-After")).isEqualTo("11");
    }

    @SuppressWarnings("unchecked")
    @Test
    void givenRedisUnavailable_whenFilter_thenFailsOpenAndPassesThrough() throws Exception {
        RemoteBucketBuilder<byte[]> builder = mock(RemoteBucketBuilder.class);
        when(builder.build(any(byte[].class), any(Supplier.class))).thenThrow(new RuntimeException("Redis down"));
        when(proxyManager.builder()).thenReturn(builder);

        var req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req.setRemoteAddr("10.0.0.1");
        var res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        verify(filterChain).doFilter(req, res);
        assertThat(res.getStatus()).isEqualTo(200);
    }

    @Test
    void givenCFConnectingIPHeader_whenExtractIp_thenCFHeaderWins() {
        var req = new MockHttpServletRequest();
        req.addHeader("CF-Connecting-IP", "1.2.3.4");
        req.addHeader("X-Real-IP", "9.9.9.9");
        req.setRemoteAddr("127.0.0.1");

        assertThat(filter.extractClientIp(req)).isEqualTo("1.2.3.4");
    }

    @Test
    void givenNoCloudflareHeader_whenExtractIp_thenXRealIPIsUsed() {
        var req = new MockHttpServletRequest();
        req.addHeader("X-Real-IP", "5.6.7.8");
        req.setRemoteAddr("127.0.0.1");

        assertThat(filter.extractClientIp(req)).isEqualTo("5.6.7.8");
    }

    @Test
    void givenNoProxyHeaders_whenExtractIp_thenRemoteAddrIsUsed() {
        var req = new MockHttpServletRequest();
        req.setRemoteAddr("192.168.1.42");

        assertThat(filter.extractClientIp(req)).isEqualTo("192.168.1.42");
    }
}
