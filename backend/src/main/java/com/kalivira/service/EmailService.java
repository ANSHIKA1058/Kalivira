package com.kalivira.service;

public interface EmailService {

    void sendOtpEmail(String email, String otp);

    void sendMfaOtpEmail(String email, String otp);
}