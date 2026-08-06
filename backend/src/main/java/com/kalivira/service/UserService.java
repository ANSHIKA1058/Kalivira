package com.kalivira.service;
import com.kalivira.dto.LoginRequest;
import com.kalivira.dto.RegisterRequest;

public interface UserService {
    String register(RegisterRequest request);
    String login(LoginRequest request);

}
