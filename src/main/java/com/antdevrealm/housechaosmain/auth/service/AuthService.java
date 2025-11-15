package com.antdevrealm.housechaosmain.auth.service;

import com.antdevrealm.housechaosmain.auth.jwt.service.JwtService;
import com.antdevrealm.housechaosmain.auth.refreshtoken.exception.RefreshTokenInvalidException;
import com.antdevrealm.housechaosmain.auth.dto.refreshtoken.CreatedRefreshTokenDTO;
import com.antdevrealm.housechaosmain.auth.dto.refreshtoken.RotationRefreshTokenResultDTO;
import com.antdevrealm.housechaosmain.auth.refreshtoken.service.RefreshTokenService;
import com.antdevrealm.housechaosmain.auth.dto.accesstoken.AccessTokenResponseDTO;
import com.antdevrealm.housechaosmain.auth.dto.login.LoginRequestDTO;
import com.antdevrealm.housechaosmain.auth.dto.login.LoginResponseDTO;
import com.antdevrealm.housechaosmain.auth.dto.registration.RegistrationRequestDTO;
import com.antdevrealm.housechaosmain.role.model.enums.UserRole;
import com.antdevrealm.housechaosmain.role.service.RoleService;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import com.antdevrealm.housechaosmain.user.dto.UserResponseDTO;
import com.antdevrealm.housechaosmain.util.ResponseDTOMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {

    private static final String REFRESH_COOKIE = "hoc_refresh";

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Value("${security.refresh.cookie.secure:true}")
    private boolean refreshCookieSecure;

    @Autowired
    public AuthService (AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    // TODO: check if email already exists
    @Transactional
    public UserResponseDTO register(RegistrationRequestDTO dto) {

        UserEntity newEntity = mapToEntity(dto);
        newEntity.getRoles().add(this.roleService.getByRole(UserRole.USER));
        UserEntity savedEntity = userRepository.save(newEntity);

        return ResponseDTOMapper.mapToUserResponseDTO(savedEntity);
    }

    public LoginResponseDTO login(LoginRequestDTO req, HttpServletResponse res) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        UserEntity user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        CreatedRefreshTokenDTO refreshToken = refreshTokenService.create(user);
        setRefreshTokenCookie(res, refreshToken.rawToken(), refreshToken.expiresAt());

        String accessToken = jwtService.generateToken(user.getEmail());

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

        setRefreshTokenCookie(res, rotationRefreshTokenResultDTO.newRaw(), rotationRefreshTokenResultDTO.expiresAt());
        String accessToken = jwtService.generateToken(rotationRefreshTokenResultDTO.userEmail());

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
                .path("/api/users/auth")
                .maxAge(cookieMaxAge)
                .build();
        res.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse res) {
        ResponseCookie cleared = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite("Lax")
                .path("/api/users/auth")
                .maxAge(Duration.ZERO)
                .build();
        res.addHeader("Set-Cookie", cleared.toString());
    }


    private UserEntity mapToEntity(RegistrationRequestDTO dto) {
        return UserEntity.builder()
                .email(dto.email())
                .password(this.passwordEncoder.encode(dto.password()))
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
