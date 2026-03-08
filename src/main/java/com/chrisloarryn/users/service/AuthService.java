package com.chrisloarryn.users.service;

import com.chrisloarryn.users.web.dto.request.LoginRequest;
import com.chrisloarryn.users.web.dto.request.RegisterUserRequest;
import com.chrisloarryn.users.web.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterUserRequest request);

    AuthResponse login(LoginRequest request);
}
