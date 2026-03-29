package com.antdevrealm.housechaosmain.auth;

import com.antdevrealm.housechaosmain.auth.refreshtoken.exception.RefreshTokenInvalidException;
import com.antdevrealm.housechaosmain.auth.web.RefreshCookieHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshCookieHelperUTest {

    private RefreshCookieHelper helper;

    @BeforeEach
    void setUp() throws Exception {
        helper = new RefreshCookieHelper();
        var secureField = RefreshCookieHelper.class.getDeclaredField("secure");
        secureField.setAccessible(true);
        secureField.set(helper, false);
    }

    @Test
    void write_setsHttpOnlyCookieWithCorrectNameAndPath() {
        MockHttpServletResponse res = new MockHttpServletResponse();

        helper.write(res, "raw-token", Instant.now().plusSeconds(3600));

        String setCookie = res.getHeader("Set-Cookie");
        assertThat(setCookie).contains("hoc_refresh=raw-token");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Path=/api/v1/auth");
        assertThat(setCookie).contains("SameSite=Lax");
    }

    @Test
    void write_maxAgeIsPositive_whenExpiresAtIsInFuture() {
        MockHttpServletResponse res = new MockHttpServletResponse();

        helper.write(res, "raw-token", Instant.now().plusSeconds(3600));

        String setCookie = res.getHeader("Set-Cookie");
        assertThat(setCookie).contains("Max-Age=");
        String maxAgePart = setCookie.split("Max-Age=")[1].split(";")[0].trim();
        assertThat(Long.parseLong(maxAgePart)).isPositive();
    }

    @Test
    void write_maxAgeIsZero_whenExpiresAtIsInPast() {
        MockHttpServletResponse res = new MockHttpServletResponse();

        helper.write(res, "raw-token", Instant.now().minusSeconds(60));

        String setCookie = res.getHeader("Set-Cookie");
        assertThat(setCookie).contains("Max-Age=0");
    }

    @Test
    void extract_returnsRawToken_whenCookiePresent() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new jakarta.servlet.http.Cookie("hoc_refresh", "my-raw-token"));

        String result = helper.extract(req);

        assertThat(result).isEqualTo("my-raw-token");
    }

    @Test
    void extract_throwsRefreshTokenInvalidException_whenCookieAbsent() {
        MockHttpServletRequest req = new MockHttpServletRequest();

        assertThatThrownBy(() -> helper.extract(req))
                .isInstanceOf(RefreshTokenInvalidException.class);
    }

    @Test
    void extract_throwsRefreshTokenInvalidException_whenCookieValueIsBlank() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new jakarta.servlet.http.Cookie("hoc_refresh", "   "));

        assertThatThrownBy(() -> helper.extract(req))
                .isInstanceOf(RefreshTokenInvalidException.class);
    }

    @Test
    void clear_setsCookieWithMaxAgeZeroAndEmptyValue() {
        MockHttpServletResponse res = new MockHttpServletResponse();

        helper.clear(res);

        String setCookie = res.getHeader("Set-Cookie");
        assertThat(setCookie).contains("hoc_refresh=");
        assertThat(setCookie).contains("Max-Age=0");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Path=/api/v1/auth");
    }
}
