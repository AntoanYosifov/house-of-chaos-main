package com.antdevrealm.housechaosmain.auth.web;

import com.antdevrealm.housechaosmain.auth.refreshtoken.exception.RefreshTokenInvalidException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class RefreshCookieHelper {

    static final String REFRESH_COOKIE = "hoc_refresh";

    @Value("${security.refresh.cookie.secure:true}")
    private boolean secure;

    public void write(HttpServletResponse res, String rawToken, Instant expiresAt) {
        Duration cookieMaxAge = Duration.between(Instant.now(), expiresAt);
        if (cookieMaxAge.isNegative()) {
            cookieMaxAge = Duration.ZERO;
        }
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, rawToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(cookieMaxAge)
                .build();
        res.addHeader("Set-Cookie", cookie.toString());
    }

    public String extract(HttpServletRequest req) {
        if (req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                if (REFRESH_COOKIE.equals(cookie.getName())) {
                    String value = cookie.getValue();
                    if (value != null && !value.isBlank()) {
                        return value;
                    }
                }
            }
        }
        throw new RefreshTokenInvalidException("Refresh token is invalid");
    }

    public void clear(HttpServletResponse res) {
        ResponseCookie cleared = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();
        res.addHeader("Set-Cookie", cleared.toString());
    }
}
