package com.antdevrealm.housechaosmain.auth.service;

import com.antdevrealm.housechaosmain.auth.dto.token.IssuedTokenDTO;
import com.antdevrealm.housechaosmain.auth.dto.token.LoginIssuedTokenDTO;
import com.antdevrealm.housechaosmain.auth.dto.login.LoginRequestDTO;
import com.antdevrealm.housechaosmain.auth.jwt.service.JwtService;
import com.antdevrealm.housechaosmain.auth.model.HOCUserDetails;
import com.antdevrealm.housechaosmain.auth.refreshtoken.dto.CreatedRefreshTokenDTO;
import com.antdevrealm.housechaosmain.auth.refreshtoken.dto.RotationRefreshTokenResultDTO;
import com.antdevrealm.housechaosmain.auth.refreshtoken.service.RefreshTokenService;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import com.antdevrealm.housechaosmain.util.ResponseDTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final HOCUserDetailsService hocUserDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           UserRepository userRepository,
                           HOCUserDetailsService hocUserDetailsService,
                           RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.hocUserDetailsService = hocUserDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public LoginIssuedTokenDTO login(LoginRequestDTO req) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        HOCUserDetails principal = (HOCUserDetails) authentication.getPrincipal();

        UserEntity user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        CreatedRefreshTokenDTO refreshToken = refreshTokenService.create(user);
        String accessToken = jwtService.generateToken(principal);

        IssuedTokenDTO issuedToken = new IssuedTokenDTO(
                accessToken,
                refreshToken.rawToken(),
                refreshToken.expiresAt(),
                jwtService.ttlSeconds()
        );

        return new LoginIssuedTokenDTO(issuedToken, ResponseDTOMapper.mapToUserResponseDTO(user));
    }

    @Override
    public IssuedTokenDTO refresh(String rawRefreshToken) {
        RotationRefreshTokenResultDTO rotation = refreshTokenService.rotateInPlace(rawRefreshToken);

        HOCUserDetails principal = (HOCUserDetails) hocUserDetailsService.loadUserByUsername(rotation.userEmail());
        String accessToken = jwtService.generateToken(principal);

        return new IssuedTokenDTO(
                accessToken,
                rotation.newRaw(),
                rotation.expiresAt(),
                jwtService.ttlSeconds()
        );
    }

    @Override
    public void logout(String rawRefreshToken) {
        refreshTokenService.deleteByTokenHash(rawRefreshToken);
    }
}
