package com.antdevrealm.housechaosmain.user.web;

import com.antdevrealm.housechaosmain.auth.model.HOCUserDetails;
import com.antdevrealm.housechaosmain.user.service.UserService;
import com.antdevrealm.housechaosmain.user.web.dto.UserResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDTO> profile(@AuthenticationPrincipal HOCUserDetails principal) {
        UserResponseDTO userResponseDTO = this.userService.getById(principal.getUserId());

        return ResponseEntity.ok(userResponseDTO);
    }
}
