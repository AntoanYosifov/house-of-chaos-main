package com.antdevrealm.housechaosmain.user.web;

import com.antdevrealm.housechaosmain.auth.dto.registration.RegistrationRequestDTO;
import com.antdevrealm.housechaosmain.auth.model.HOCUserDetails;
import com.antdevrealm.housechaosmain.user.service.UserService;
import com.antdevrealm.housechaosmain.user.dto.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
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
    public ResponseEntity<UserResponseDTO> profile(@AuthenticationPrincipal HOCUserDetails principal) {
        UserResponseDTO userResponseDTO = this.userService.getById(principal.getUserId());

        return ResponseEntity.ok(userResponseDTO);
    }
}
