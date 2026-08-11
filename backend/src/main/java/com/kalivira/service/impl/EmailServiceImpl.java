package com.kalivira.service.impl;

import com.kalivira.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Kalivira - Email Verification OTP");

        message.setText(
                "Hello,\n\n" +
                        "Your Kalivira email verification OTP is:\n\n" +
                        otp + "\n\n" +
                        "This OTP is valid for 5 minutes.\n\n" +
                        "If you did not create this account, please ignore this email.\n\n" +
                        "Regards,\n" +
                        "Kalivira Team"
        );

        mailSender.send(message);
    }

    @Override
    public void sendMfaOtpEmail(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Kalivira - MFA Login OTP");

        message.setText(
                "Hello,\n\n" +
                        "Your Kalivira MFA login OTP is:\n\n" +
                        otp + "\n\n" +
                        "This OTP is valid for 5 minutes.\n\n" +
                        "If you did not attempt to login, please secure your account.\n\n" +
                        "Regards,\n" +
                        "Kalivira Team"
        );

        mailSender.send(message);
    }
}