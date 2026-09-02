package com.insurance.portal.auth.service;

import com.insurance.portal.auth.dto.request.LoginRequest;
import com.insurance.portal.auth.dto.request.RegisterRequest;
import com.insurance.portal.auth.dto.response.LoginResponse;
import com.insurance.portal.auth.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest registerRequest);

    LoginResponse login(LoginRequest loginRequest);
}
