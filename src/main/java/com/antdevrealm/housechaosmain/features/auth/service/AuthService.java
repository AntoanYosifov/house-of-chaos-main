package com.antdevrealm.housechaosmain.features.auth.service;

import com.antdevrealm.housechaosmain.features.auth.model.dto.CreatedRefreshToken;
import com.antdevrealm.housechaosmain.features.auth.web.dto.AccessTokenResponse;
import com.antdevrealm.housechaosmain.features.auth.web.dto.LoginRequest;
import com.antdevrealm.housechaosmain.features.auth.web.dto.RegistrationRequest;
import com.antdevrealm.housechaosmain.features.auth.web.dto.RegistrationResponse;
import com.antdevrealm.housechaosmain.features.user.model.entity.UserEntity;
import com.antdevrealm.housechaosmain.features.user.model.enums.UserRole;
import com.antdevrealm.housechaosmain.features.user.repository.UserRepository;
import com.antdevrealm.housechaosmain.infrastructure.security.jwt.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

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

    public RegistrationResponse register(RegistrationRequest dto) {

        UserEntity newEntity = mapToEntity(dto);
        UserEntity savedEntity = userRepository.save(newEntity);

        return mapToResponseDto(savedEntity);
    }

    public AccessTokenResponse login(LoginRequest req, HttpServletResponse res) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        UserEntity user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        CreatedRefreshToken refreshToken = refreshTokenService.create(user);
        setRefreshTokenCookie(res, refreshToken);

        String accessToken = jwtService.generateToken(user.getEmail());

        return new AccessTokenResponse(accessToken, "Bearer", jwtService.ttlSeconds());
    }


    private void setRefreshTokenCookie(HttpServletResponse res, CreatedRefreshToken refreshToken) {
        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_COOKIE, refreshToken.rawToken())
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.between(LocalDateTime.now(), refreshToken.expiresAt()))
                .build();

        res.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }

    private static RegistrationResponse mapToResponseDto(UserEntity savedEntity) {
        return new RegistrationResponse(savedEntity.getId(),
                savedEntity.getEmail(),
                savedEntity.isActive(),
                savedEntity.getCreatedOn(),
                savedEntity.getUpdatedAt());
    }

    private UserEntity mapToEntity(RegistrationRequest dto) {
        return UserEntity.builder()
                .email(dto.email())
                .password(this.passwordEncoder.encode(dto.password()))
                .role(UserRole.USER)
                .active(true)
                .createdOn(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
