package com.kalivira.controller;

import com.kalivira.dto.RegisterRequest;
import com.kalivira.dto.LoginRequest;
import com.kalivira.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
public class AutoController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {

        String response = userService.login(request);

        if (response.equals(
                "Invalid email or password. Please try again.")) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {

        String response = userService.verifyOtp(email, otp);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(
            @RequestParam String email) {

        String response = userService.resendOtp(email);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestParam String email) {

        String response = userService.forgotPassword(email);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {

        String response = userService.resetPassword(
                email,
                otp,
                newPassword
        );

        if ("Password reset successfully.".equals(response)) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @PostMapping("/verify-mfa")
    public ResponseEntity<String> verifyMfa(
            @RequestParam String email,
            @RequestParam String otp) {

        String response = userService.verifyMfa(email, otp);

        if (response.equals("Email not found") ||
                response.equals("MFA OTP not found. Please login again.") ||
                response.equals("MFA OTP expired. Please login again.") ||
                response.equals("Invalid MFA OTP")) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }
}