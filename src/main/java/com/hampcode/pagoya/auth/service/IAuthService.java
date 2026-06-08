package com.hampcode.pagoya.auth.service;

import com.hampcode.pagoya.auth.dto.AuthResponse;
import com.hampcode.pagoya.auth.dto.LoginRequest;

public interface IAuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String refreshToken);
    void logout(String refreshToken);
    void logoutAll(String email);
}
