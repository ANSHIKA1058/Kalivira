package com.kalivira.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleController {

    @GetMapping("/api/user/profile")
    public String userProfile(Authentication authentication) {

        return "Welcome User : " + authentication.getName();
    }

    @GetMapping("/api/admin/dashboard")
    public String adminDashboard(Authentication authentication) {

        return "Welcome Admin : " + authentication.getName();
    }
}