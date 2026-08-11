package com.kalivira.controller;

import com.kalivira.dto.RegisterRequest;
import com.kalivira.dto.LoginRequest;
import com.kalivira.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public String login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {

        String response = userService.verifyOtp(email, otp);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-mfa")
    public ResponseEntity<String> verifyMfa(
            @RequestParam String email,
            @RequestParam String otp) {

        String response = userService.verifyMfa(email, otp);

        return ResponseEntity.ok(response);
    }
}