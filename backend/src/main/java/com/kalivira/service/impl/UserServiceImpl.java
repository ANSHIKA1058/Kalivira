package com.kalivira.service.impl;

import com.kalivira.dto.RegisterRequest;
import com.kalivira.dto.LoginRequest;
import com.kalivira.entity.UserEntity;
import com.kalivira.repository.UserRepository;
import com.kalivira.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.kalivira.util.JwtUtil;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.regex.Pattern;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String register(RegisterRequest request) {

        // Name validation
        if (request.getName() == null ||
                request.getName().trim().isEmpty()) {
            return "Name is required";
        }

        // Email validation
        if (request.getEmail() == null ||
                request.getEmail().trim().isEmpty()) {
            return "Email is required";
        }

        String email = request.getEmail().trim();

        String emailRegex =
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        if (!Pattern.matches(emailRegex, email)) {
            return "Invalid email format";
        }

        // Checking duplicate email
        if (userRepository.existsByEmail(email)) {
            return "Email already exists";
        }

        // Password validation
        String password = request.getPassword();

        if (password == null || password.isEmpty()) {
            return "Password is required";
        }

        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }

        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain an uppercase letter";
        }

        if (!password.matches(".*[a-z].*")) {
            return "Password must contain a lowercase letter";
        }

        if (!password.matches(".*[0-9].*")) {
            return "Password must contain a number";
        }

        if (!password.matches(".*[^A-Za-z0-9].*")) {
            return "Password must contain a special character";
        }

        // Create new user
        UserEntity user = new UserEntity();

        user.setName(request.getName().trim());
        user.setEmail(email);

        // BCrypt hashing
        user.setPassword(
                passwordEncoder.encode(password)
        );

        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());

        // Save to DB
        userRepository.save(user);

        return "User Registered Successfully";
    }


    @Override
    public String login(LoginRequest request){

        //Email search
        Optional<UserEntity> userOptional = userRepository.findByEmail(request.getEmail());

        if(userOptional.isEmpty()){
            return "Email not found";
        }
        UserEntity user = userOptional.get();

        //password verify
        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            return "Invalid Password";
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return token;
    }
}
