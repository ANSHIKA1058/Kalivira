package com.kalivira.service.impl;

import com.kalivira.dto.RegisterRequest;
import com.kalivira.entity.UserEntity;
import com.kalivira.repository.UserRepository;
import com.kalivira.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    @Override
    public String register(RegisterRequest request){

        //checking email exists or not
        if(userRepository.existsByEmail(request.getEmail())){
            return "Email already exists";
        }

        //new user creation
        UserEntity user = new UserEntity();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());


        //save to db
        userRepository.save(user);
        return "User Registered Successfully";

    }
}
