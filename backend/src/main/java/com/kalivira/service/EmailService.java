package com.kalivira.service;
public interface EmailService {
    // Registration email verification OTP
    void sendOtpEmail(String email, String otp);
    // Login MFA OTP
    void sendMfaOtpEmail(String email, String otp);
    // Forgot password OTP
    void sendPasswordResetOtpEmail(String email, String otp);
    // File encryption password recovery OTP
    void sendFileRecoveryOtpEmail(String email, String otp);
}