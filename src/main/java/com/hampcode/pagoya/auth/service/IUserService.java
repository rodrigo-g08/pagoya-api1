package com.hampcode.pagoya.auth.service;

import com.hampcode.pagoya.auth.dto.RegisterResponse;
import com.hampcode.pagoya.auth.dto.RegisterUserRequest;

public interface IUserService {
    RegisterResponse register(RegisterUserRequest request);
}
