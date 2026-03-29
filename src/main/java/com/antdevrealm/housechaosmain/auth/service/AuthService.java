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
import com.antdevrealm.housechaosmain.auth.web.RefreshCookieHelper;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import com.antdevrealm.housechaosmain.util.ResponseDTOMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final HOCUserDetailsService hocUserDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final RefreshCookieHelper refreshCookieHelper;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserRepository userRepository,
                       HOCUserDetailsService hocUserDetailsService,
                       RefreshTokenService refreshTokenService,
                       RefreshCookieHelper refreshCookieHelper) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.hocUserDetailsService = hocUserDetailsService;
        this.refreshTokenService = refreshTokenService;
        this.refreshCookieHelper = refreshCookieHelper;
    }

    public LoginResponseDTO login(LoginRequestDTO req, HttpServletResponse res) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        HOCUserDetails hocUserDetails = (HOCUserDetails) authentication.getPrincipal();

        UserEntity user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        CreatedRefreshTokenDTO refreshToken = refreshTokenService.create(user);
        refreshCookieHelper.write(res, refreshToken.rawToken(), refreshToken.expiresAt());

        String accessToken = jwtService.generateToken(hocUserDetails);

        AccessTokenResponseDTO tokenResponseDTO = new AccessTokenResponseDTO(accessToken, "Bearer", jwtService.ttlSeconds());
        return new LoginResponseDTO(tokenResponseDTO, ResponseDTOMapper.mapToUserResponseDTO(user));
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = refreshCookieHelper.extract(request);
        if (rawToken == null || rawToken.isBlank()) {
            throw new RefreshTokenInvalidException("Refresh token is invalid");
        }

        this.refreshTokenService.deleteByTokenHash(rawToken);
        refreshCookieHelper.clear(response);
    }

    public AccessTokenResponseDTO refreshToken(HttpServletRequest req, HttpServletResponse res) {
        String rawToken = refreshCookieHelper.extract(req);
        if (rawToken == null || rawToken.isBlank()) {
            throw new RefreshTokenInvalidException("Refresh token is invalid");
        }

        RotationRefreshTokenResultDTO rotationRefreshTokenResultDTO = refreshTokenService.rotateInPlace(rawToken);

        HOCUserDetails userDetails = (HOCUserDetails) hocUserDetailsService.loadUserByUsername(
                rotationRefreshTokenResultDTO.userEmail());

        refreshCookieHelper.write(res, rotationRefreshTokenResultDTO.newRaw(), rotationRefreshTokenResultDTO.expiresAt());
        String accessToken = jwtService.generateToken(userDetails);

        return new AccessTokenResponseDTO(accessToken, "Bearer", jwtService.ttlSeconds());
    }
}
