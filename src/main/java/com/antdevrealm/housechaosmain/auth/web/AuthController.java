package com.antdevrealm.housechaosmain.auth.web;

import com.antdevrealm.housechaosmain.auth.dto.IssuedTokenDTO;
import com.antdevrealm.housechaosmain.auth.dto.LoginIssuedTokenDTO;
import com.antdevrealm.housechaosmain.auth.dto.accesstoken.AccessTokenResponseDTO;
import com.antdevrealm.housechaosmain.auth.dto.login.LoginRequestDTO;
import com.antdevrealm.housechaosmain.auth.dto.login.LoginResponseDTO;
import com.antdevrealm.housechaosmain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshCookieHelper refreshCookieHelper;

    @Autowired
    public AuthController(AuthService authService, RefreshCookieHelper refreshCookieHelper) {
        this.authService = authService;
        this.refreshCookieHelper = refreshCookieHelper;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO req, HttpServletResponse res) {
        LoginIssuedTokenDTO result = authService.login(req);
        refreshCookieHelper.write(res, result.issuedToken().rawRefreshToken(), result.issuedToken().refreshExpiresAt());
        AccessTokenResponseDTO tokenResponse = new AccessTokenResponseDTO(
                result.issuedToken().accessToken(), "Bearer", result.issuedToken().accessTtlSeconds());
        return ResponseEntity.ok(new LoginResponseDTO(tokenResponse, result.user()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest req, HttpServletResponse res) {
        String rawToken = refreshCookieHelper.extract(req);
        authService.logout(rawToken);
        refreshCookieHelper.clear(res);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponseDTO> refresh(HttpServletRequest req, HttpServletResponse res) {
        String rawToken = refreshCookieHelper.extract(req);
        IssuedTokenDTO issued = authService.refresh(rawToken);
        refreshCookieHelper.write(res, issued.rawRefreshToken(), issued.refreshExpiresAt());
        return ResponseEntity.ok(new AccessTokenResponseDTO(issued.accessToken(), "Bearer", issued.accessTtlSeconds()));
    }
}
