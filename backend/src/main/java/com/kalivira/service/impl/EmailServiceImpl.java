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

        try {
            mailSender.send(message);
            System.out.println("OTP EMAIL SENT SUCCESSFULLY TO: " + email);
        } catch (Exception e) {
            System.out.println("OTP EMAIL FAILED!");
            e.printStackTrace();
            throw e;
        }
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

        try {
            mailSender.send(message);
            System.out.println("OTP EMAIL SENT SUCCESSFULLY TO: " + email);
        } catch (Exception e) {
            System.out.println("OTP EMAIL FAILED!");
            e.printStackTrace();
            throw e;
        }
    }


    @Override
    public void sendPasswordResetOtpEmail(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Kalivira - Password Reset OTP");

        message.setText(
                "Hello,\n\n" +
                        "We received a request to reset your Kalivira account password.\n\n" +
                        "Your password reset OTP is:\n\n" +
                        otp + "\n\n" +
                        "This OTP is valid for 5 minutes.\n\n" +
                        "If you did not request a password reset, please ignore this email.\n\n" +
                        "Regards,\n" +
                        "Kalivira Team"
        );

        try {
            mailSender.send(message);
            System.out.println("OTP EMAIL SENT SUCCESSFULLY TO: " + email);
        } catch (Exception e) {
            System.out.println("OTP EMAIL FAILED!");
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void sendFileRecoveryOtpEmail(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Kalivira - File Encryption Recovery OTP");

        message.setText(
                "Hello,\n\n" +
                        "We received a request to reset your Kalivira file encryption password.\n\n" +
                        "Your recovery OTP is:\n\n" +
                        otp + "\n\n" +
                        "This OTP is valid for 5 minutes.\n\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "Regards,\n" +
                        "Kalivira Team"
        );

        try {
            mailSender.send(message);
            System.out.println("FILE RECOVERY OTP SENT TO: " + email);
        } catch (Exception e) {
            System.out.println("FILE RECOVERY OTP EMAIL FAILED!");
            e.printStackTrace();
            throw e;
        }
    }
}