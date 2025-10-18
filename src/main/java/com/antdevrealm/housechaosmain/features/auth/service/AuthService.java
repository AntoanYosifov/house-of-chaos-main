package com.antdevrealm.housechaosmain.features.auth.service;

import com.antdevrealm.housechaosmain.features.auth.model.dto.CreatedRefreshToken;
import com.antdevrealm.housechaosmain.features.auth.model.dto.RotationRefreshTokenResult;
import com.antdevrealm.housechaosmain.features.auth.web.dto.*;
import com.antdevrealm.housechaosmain.features.user.model.entity.UserEntity;
import com.antdevrealm.housechaosmain.features.user.model.enums.UserRole;
import com.antdevrealm.housechaosmain.features.user.repository.UserRepository;
import com.antdevrealm.housechaosmain.infrastructure.security.jwt.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {

    private static final String REFRESH_COOKIE = "hoc_refresh";

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Value("${security.refresh.cookie.secure:true}")
    private boolean refreshCookieSecure;

    @Autowired
    public AuthService (AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    // TODO: check if email already exists
    @Transactional
    public UserResponseDTO register(RegistrationRequestDTO dto) {

        UserEntity newEntity = mapToEntity(dto);
        UserEntity savedEntity = userRepository.save(newEntity);

        return mapToUserResponseDto(savedEntity);
    }

    public LoginResponseDTO login(LoginRequestDTO req, HttpServletResponse res) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        UserEntity user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        CreatedRefreshToken refreshToken = refreshTokenService.create(user);
        setRefreshTokenCookie(res, refreshToken.rawToken(), refreshToken.expiresAt());

        String accessToken = jwtService.generateToken(user.getEmail());

        AccessTokenResponseDTO tokenResponseDTO = new AccessTokenResponseDTO(accessToken, "Bearer", jwtService.ttlSeconds());
        return new LoginResponseDTO(tokenResponseDTO, mapToUserResponseDto(user));

    }

    public AccessTokenResponseDTO refreshToken(HttpServletRequest req, HttpServletResponse res) {
        String rawToken = readCookie(req);
        if(rawToken == null || rawToken.isBlank()) {
            throw unauthorized();
        }

        RotationRefreshTokenResult rotationRefreshTokenResult = refreshTokenService.rotateInPlace(rawToken);

        setRefreshTokenCookie(res, rotationRefreshTokenResult.newRaw(), rotationRefreshTokenResult.expiresAt());
        String accessToken = jwtService.generateToken(rotationRefreshTokenResult.userEmail());

        return new AccessTokenResponseDTO(accessToken, "Bearer", jwtService.ttlSeconds());
    }

    private String readCookie(HttpServletRequest req) {
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
                .path("/api/users/auth")
                .maxAge(cookieMaxAge)
                .build();
        res.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }

    private static UserResponseDTO mapToUserResponseDto(UserEntity savedEntity) {
        return new UserResponseDTO(savedEntity.getId(),
                savedEntity.getEmail(),
                savedEntity.isActive(),
                savedEntity.getCreatedOn(),
                savedEntity.getUpdatedAt());
    }

    private UserEntity mapToEntity(RegistrationRequestDTO dto) {
        return UserEntity.builder()
                .email(dto.email())
                .password(this.passwordEncoder.encode(dto.password()))
                .role(UserRole.USER)
                .active(true)
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    //TODO: Create and throw custom exception and handle it in a ControllerAdvice to map to 401
    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
}
