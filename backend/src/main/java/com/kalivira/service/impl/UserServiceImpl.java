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
import com.kalivira.service.EmailService;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;


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

        // Duplicate email check
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

        // Create user
        UserEntity user = new UserEntity();

        user.setName(request.getName().trim());
        user.setEmail(email);

        // BCrypt password hashing
        user.setPassword(passwordEncoder.encode(password));

        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());

        // Email verification
        user.setEmailVerified(false);

        // Generate 6-digit OTP
        String otp = String.valueOf(
                (int) (Math.random() * 900000) + 100000
        );

        // OTP expiry = 5 minutes
        user.setVerificationOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        // Save user
        userRepository.save(user);

        // Send OTP email
        emailService.sendOtpEmail(email, otp);

        return "User registered successfully. OTP sent to your email.";
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


    @Override
    public String verifyOtp(String email, String otp) {

        Optional<UserEntity> userOptional =
                userRepository.findByEmail(email);

        // Email doesn't exist
        if (userOptional.isEmpty()) {
            return "Email not found";
        }

        UserEntity user = userOptional.get();

        // Already verified
        if (user.isEmailVerified()) {
            return "Email already verified";
        }

        // OTP doesn't exist
        if (user.getVerificationOtp() == null) {
            return "OTP not found. Please register again.";
        }

        // OTP expired
        if (user.getOtpExpiry() == null ||
                LocalDateTime.now().isAfter(user.getOtpExpiry())) {

            return "OTP expired. Please request a new OTP.";
        }

        // WRONG OTP
        if (!user.getVerificationOtp().equals(otp)) {
            return "Invalid OTP";
        }

        // Correct OTP
        user.setEmailVerified(true);

        // OTP ko clear kar do after successful verification
        user.setVerificationOtp(null);
        user.setOtpExpiry(null);

        userRepository.save(user);

        return "Email verified successfully";
    }
}
