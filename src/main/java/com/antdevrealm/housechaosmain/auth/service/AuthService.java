package com.antdevrealm.housechaosmain.auth.service;

import com.antdevrealm.housechaosmain.auth.dto.IssuedTokenDTO;
import com.antdevrealm.housechaosmain.auth.dto.LoginIssuedTokenDTO;
import com.antdevrealm.housechaosmain.auth.dto.login.LoginRequestDTO;

public interface AuthService {

    LoginIssuedTokenDTO login(LoginRequestDTO req);

    IssuedTokenDTO refresh(String rawRefreshToken);

    void logout(String rawRefreshToken);
}
