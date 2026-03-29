package com.antdevrealm.housechaosmain.auth.service;

import com.antdevrealm.housechaosmain.auth.dto.login.LoginRequestDTO;
import com.antdevrealm.housechaosmain.auth.dto.login.LoginResultDTO;
import com.antdevrealm.housechaosmain.auth.dto.token.TokenIssuanceResultDTO;

public interface AuthService {

    LoginResultDTO login(LoginRequestDTO req);

    TokenIssuanceResultDTO refresh(String rawRefreshToken);

    void logout(String rawRefreshToken);
}
