package com.antdevrealm.housechaosmain.features.user.web;

import com.antdevrealm.housechaosmain.features.user.web.dto.LoginRequest;
import com.antdevrealm.housechaosmain.features.user.web.dto.RegistrationRequest;
import com.antdevrealm.housechaosmain.features.user.web.dto.RegistrationResponse;
import com.antdevrealm.housechaosmain.features.user.service.UserService;
import com.antdevrealm.housechaosmain.features.user.web.dto.TokenResponse;
import com.antdevrealm.housechaosmain.infrastructure.security.jwt.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/users")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@RequestBody RegistrationRequest req) {
        RegistrationResponse registrationResponse = userService.register(req);

        URI uriLocation = URI.create("/users/" + registrationResponse.id());

        return ResponseEntity.created(uriLocation).body(registrationResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        String token = jwtService.generateToken(req.email());

        TokenResponse tokenResponse = new TokenResponse(token, "Bearer", jwtService.ttlSeconds());
        return ResponseEntity.ok(tokenResponse);
    }

}
