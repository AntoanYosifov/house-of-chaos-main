package com.antdevrealm.housechaosmain.auth.service;

import com.antdevrealm.housechaosmain.auth.dto.accesstoken.AccessTokenResponseDTO;
import com.antdevrealm.housechaosmain.auth.dto.login.LoginRequestDTO;
import com.antdevrealm.housechaosmain.auth.dto.login.LoginResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO req, HttpServletResponse res);

    void logout(HttpServletRequest request, HttpServletResponse response);

    AccessTokenResponseDTO refreshToken(HttpServletRequest req, HttpServletResponse res);
}
