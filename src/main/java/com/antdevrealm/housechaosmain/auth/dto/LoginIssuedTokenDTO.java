package com.antdevrealm.housechaosmain.auth.dto;

import com.antdevrealm.housechaosmain.user.dto.UserResponseDTO;

public record LoginIssuedTokenDTO(IssuedTokenDTO issuedToken, UserResponseDTO user) {}
