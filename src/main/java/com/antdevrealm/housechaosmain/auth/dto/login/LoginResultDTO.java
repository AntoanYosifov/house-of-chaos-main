package com.antdevrealm.housechaosmain.auth.dto.login;

import com.antdevrealm.housechaosmain.auth.dto.token.TokenIssuanceResultDTO;
import com.antdevrealm.housechaosmain.user.dto.UserResponseDTO;

public record LoginResultDTO(TokenIssuanceResultDTO issuedToken, UserResponseDTO user) {}
