package com.kalivira.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test(Authentication authentication){

        return "Logged in user: "+authentication.getName() ;

    }

}