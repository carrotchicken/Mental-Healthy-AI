package com.amha.service;

import com.amha.dto.LoginRequest;
import com.amha.dto.LoginResponse;
import com.amha.dto.RegisterRequest;

public interface UserService {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
    void logout(String token);
}
