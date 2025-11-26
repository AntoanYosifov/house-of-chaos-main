package com.antdevrealm.housechaosmain.user.web;

import com.antdevrealm.housechaosmain.user.dto.RegistrationRequestDTO;
import com.antdevrealm.housechaosmain.user.dto.UpdateProfileRequestDTO;
import com.antdevrealm.housechaosmain.user.dto.UserResponseDTO;
import com.antdevrealm.housechaosmain.user.service.UserService;
import com.antdevrealm.housechaosmain.util.PrincipalUUIDExtractor;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody @Valid RegistrationRequestDTO req) {
        UserResponseDTO userResponseDTO = userService.register(req);

        URI uriLocation = URI.create("/api/users/" + userResponseDTO.id());

        return ResponseEntity.created(uriLocation).body(userResponseDTO);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDTO> profile(@AuthenticationPrincipal Jwt principal) {
        UUID userId = PrincipalUUIDExtractor.extract(principal);

        UserResponseDTO userResponseDTO = this.userService.getById(userId);

        return ResponseEntity.ok(userResponseDTO);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponseDTO> profile(@AuthenticationPrincipal Jwt principal,
                                                   @RequestBody UpdateProfileRequestDTO req) {

        UUID userId = PrincipalUUIDExtractor.extract(principal);
        UserResponseDTO userResponseDTO = this.userService.update(userId, req);

        return ResponseEntity.ok(userResponseDTO);
    }
}
