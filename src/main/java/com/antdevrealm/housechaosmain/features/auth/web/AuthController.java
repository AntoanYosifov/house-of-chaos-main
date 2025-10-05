package com.antdevrealm.housechaosmain.features.auth.web;

import com.antdevrealm.housechaosmain.features.auth.service.AuthService;
import com.antdevrealm.housechaosmain.features.auth.web.dto.LoginRequest;
import com.antdevrealm.housechaosmain.features.auth.web.dto.RegistrationRequest;
import com.antdevrealm.housechaosmain.features.auth.web.dto.RegistrationResponse;
import com.antdevrealm.housechaosmain.features.user.service.UserService;
import com.antdevrealm.housechaosmain.features.auth.web.dto.TokenResponse;
import com.antdevrealm.housechaosmain.infrastructure.security.jwt.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthService authService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@RequestBody @Valid RegistrationRequest req) {
        RegistrationResponse registrationResponse = authService.register(req);

        URI uriLocation = URI.create("/users/" + registrationResponse.id());

        return ResponseEntity.created(uriLocation).body(registrationResponse);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        String token = jwtService.generateToken(req.email());

        TokenResponse tokenResponse = new TokenResponse(token, "Bearer", jwtService.ttlSeconds());
        return ResponseEntity.ok(tokenResponse);
    }

    // Test authenticated end point
    @GetMapping("/protected")
    public ResponseEntity<String> getProtected() {
        return ResponseEntity.ok("You got here");
    }

    // Test free endpoint
    @GetMapping("/free")
    public ResponseEntity<String> getFree() {
        return ResponseEntity.ok("Hello Sarah!");
    }

}
