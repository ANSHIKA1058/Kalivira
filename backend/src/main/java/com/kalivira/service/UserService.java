package com.kalivira.service;
import com.kalivira.dto.LoginRequest;
import com.kalivira.dto.RegisterRequest;

public interface UserService {
    String register(RegisterRequest request);
    String login(LoginRequest request);
    String verifyOtp(String email, String otp);
    String verifyMfa(String email, String otp);
    String resendOtp(String email);
    String forgotPassword(String email);
    String resetPassword(String email, String otp, String newPassword);
}