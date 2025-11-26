package com.antdevrealm.housechaosmain.auth.service;

import com.antdevrealm.housechaosmain.auth.dto.accesstoken.AccessTokenResponseDTO;
import com.antdevrealm.housechaosmain.auth.dto.login.LoginRequestDTO;
import com.antdevrealm.housechaosmain.auth.dto.login.LoginResponseDTO;
import com.antdevrealm.housechaosmain.auth.refreshtoken.dto.CreatedRefreshTokenDTO;
import com.antdevrealm.housechaosmain.auth.refreshtoken.dto.RotationRefreshTokenResultDTO;
import com.antdevrealm.housechaosmain.auth.jwt.service.JwtService;
import com.antdevrealm.housechaosmain.auth.model.HOCUserDetails;
import com.antdevrealm.housechaosmain.auth.refreshtoken.exception.RefreshTokenInvalidException;
import com.antdevrealm.housechaosmain.auth.refreshtoken.service.RefreshTokenService;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import com.antdevrealm.housechaosmain.util.ResponseDTOMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {

    private static final String REFRESH_COOKIE = "hoc_refresh";

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final HOCUserDetailsService hocUserDetailsService;

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Value("${security.refresh.cookie.secure:true}")
    private boolean refreshCookieSecure;

    @Autowired
    public AuthService (AuthenticationManager authenticationManager,
                        JwtService jwtService,
                        UserRepository userRepository, HOCUserDetailsService hocUserDetailsService,
                        RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.hocUserDetailsService = hocUserDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    public LoginResponseDTO login(LoginRequestDTO req, HttpServletResponse res) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        HOCUserDetails hocUserDetails = (HOCUserDetails) authentication.getPrincipal();

        UserEntity user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        CreatedRefreshTokenDTO refreshToken = refreshTokenService.create(user);
        setRefreshTokenCookie(res, refreshToken.rawToken(), refreshToken.expiresAt());

        String accessToken = jwtService.generateToken(hocUserDetails);

        AccessTokenResponseDTO tokenResponseDTO = new AccessTokenResponseDTO(accessToken, "Bearer", jwtService.ttlSeconds());
        return new LoginResponseDTO(tokenResponseDTO, ResponseDTOMapper.mapToUserResponseDTO(user));

    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = getRawTokenFromCookie(request);
        if(rawToken == null || rawToken.isBlank()) {
            throw new RefreshTokenInvalidException("Refresh token is invalid");
        }

        this.refreshTokenService.deleteByTokenHash(rawToken);
        clearRefreshCookie(response);
    }

    public AccessTokenResponseDTO refreshToken(HttpServletRequest req, HttpServletResponse res) {
        String rawToken = getRawTokenFromCookie(req);
        if(rawToken == null || rawToken.isBlank()) {
            throw new RefreshTokenInvalidException("Refresh token is invalid");
        }

        RotationRefreshTokenResultDTO rotationRefreshTokenResultDTO = refreshTokenService.rotateInPlace(rawToken);

        HOCUserDetails userDetails = (HOCUserDetails) hocUserDetailsService.loadUserByUsername(rotationRefreshTokenResultDTO.userEmail());

        setRefreshTokenCookie(res, rotationRefreshTokenResultDTO.newRaw(), rotationRefreshTokenResultDTO.expiresAt());
        String accessToken = jwtService.generateToken(userDetails);

        return new AccessTokenResponseDTO(accessToken, "Bearer", jwtService.ttlSeconds());
    }

    private String getRawTokenFromCookie(HttpServletRequest req) {
        if(req.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : req.getCookies()) {
            if(AuthService.REFRESH_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void setRefreshTokenCookie(HttpServletResponse res, String rawToken, Instant expiresAt) {
        Duration cookieMaxAge = Duration.between(Instant.now(), expiresAt);
        if(cookieMaxAge.isNegative()) {
            cookieMaxAge = Duration.ZERO;
        }
        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_COOKIE, rawToken)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(cookieMaxAge)
                .build();
        res.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse res) {
        ResponseCookie cleared = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();
        res.addHeader("Set-Cookie", cleared.toString());
    }

}
