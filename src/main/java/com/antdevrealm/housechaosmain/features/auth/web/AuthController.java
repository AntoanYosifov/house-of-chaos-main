package com.antdevrealm.housechaosmain.features.auth.web;

import com.antdevrealm.housechaosmain.features.auth.service.AuthService;
import com.antdevrealm.housechaosmain.features.auth.web.dto.AccessTokenResponse;
import com.antdevrealm.housechaosmain.features.auth.web.dto.LoginRequest;
import com.antdevrealm.housechaosmain.features.auth.web.dto.RegistrationRequest;
import com.antdevrealm.housechaosmain.features.auth.web.dto.RegistrationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@RequestBody @Valid RegistrationRequest req) {
        RegistrationResponse registrationResponse = authService.register(req);

        URI uriLocation = URI.create("/users/" + registrationResponse.id());

        return ResponseEntity.created(uriLocation).body(registrationResponse);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AccessTokenResponse> login(@RequestBody LoginRequest req, HttpServletResponse res) {
        return ResponseEntity.ok(authService.login(req, res));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(HttpServletRequest req, HttpServletResponse res) {
        AccessTokenResponse accessTokenResponse = authService.refreshToken(req, res);
        return ResponseEntity.ok(accessTokenResponse);
    }
    // mock protected endpoint for testing purposes
    @GetMapping("/protected")
    public String getProtected() {
        return "You got here";
    }

}
