package com.couple.service;

import com.couple.dto.LoginResponse;
import com.couple.dto.RegisterRequest;

public interface AuthService {
    LoginResponse register(RegisterRequest req);
    LoginResponse login(String username, String password);
}
