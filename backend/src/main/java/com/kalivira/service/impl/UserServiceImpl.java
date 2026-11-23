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
import java.security.SecureRandom;


@Service
public class UserServiceImpl implements UserService {

    private static final SecureRandom secureRandom = new SecureRandom();

    private String generateOtp() {
        return String.valueOf(100000 + secureRandom.nextInt(900000));
    }

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

        String password = request.getPassword();

        String passwordError = validatePassword(password);

        if (passwordError != null) {
            return passwordError;
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
    public String login(LoginRequest request) {

        String genericError = "Invalid email or password. Please try again.";

        if (request.getEmail() == null || request.getPassword() == null) {
            return genericError;
        }

        Optional<UserEntity> userOptional =
                userRepository.findByEmail(request.getEmail().trim());

        if (userOptional.isEmpty()) {
            return genericError;
        }

        UserEntity user = userOptional.get();

        // Use the same error for an unverified account
        if (!user.isEmailVerified()) {
            return genericError;
        }

        // Check password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            return genericError;
        }

        // Generate MFA OTP
        String mfaOtp = generateOtp();

        user.setMfaOtp(mfaOtp);
        user.setMfaOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        // Send MFA OTP
        emailService.sendMfaOtpEmail(user.getEmail(), mfaOtp);

        return "Password verified. MFA OTP sent to your email.";
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

    @Override
    public String resendOtp(String email) {

        Optional<UserEntity> userOptional =
                userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return "Email not found";
        }

        UserEntity user = userOptional.get();

        if (user.isEmailVerified()) {
            return "Email already verified";
        }

        // Generate new 6-digit OTP
        String otp = String.valueOf(
                (int) (Math.random() * 900000) + 100000
        );

        // Update OTP and expiry
        user.setVerificationOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        // Send new OTP
        emailService.sendOtpEmail(email, otp);

        return "New OTP sent to your email";
    }


    @Override
    public String forgotPassword(String email) {

        String genericMessage =
                "If an account exists for that email, a password reset OTP has been sent.";

        if (email == null || email.trim().isEmpty()) {
            return genericMessage;
        }

        Optional<UserEntity> userOptional =
                userRepository.findByEmail(email.trim());

        // Do not reveal whether the email exists
        if (userOptional.isEmpty()) {
            return genericMessage;
        }

        UserEntity user = userOptional.get();

        // Do not send reset OTP to an unverified account
        if (!user.isEmailVerified()) {
            return genericMessage;
        }

        // Generate a separate password-reset OTP
        String otp = generateOtp();

        user.setPasswordResetOtp(otp);
        user.setPasswordResetOtpExpiry(
                LocalDateTime.now().plusMinutes(5)
        );

        userRepository.save(user);

        // Send password-reset email
        emailService.sendPasswordResetOtpEmail(user.getEmail(), otp);

        return genericMessage;
    }


    @Override
    public String resetPassword(
            String email,
            String otp,
            String newPassword) {

        Optional<UserEntity> userOptional =
                userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return "Invalid or expired password reset OTP.";
        }

        UserEntity user = userOptional.get();

        // Check whether reset OTP exists
        if (user.getPasswordResetOtp() == null) {
            return "Invalid or expired password reset OTP.";
        }

        // Check OTP expiry
        if (user.getPasswordResetOtpExpiry() == null ||
                LocalDateTime.now().isAfter(user.getPasswordResetOtpExpiry())) {

            user.setPasswordResetOtp(null);
            user.setPasswordResetOtpExpiry(null);
            userRepository.save(user);

            return "Invalid or expired password reset OTP.";
        }

        // Verify OTP
        if (!user.getPasswordResetOtp().equals(otp)) {
            return "Invalid or expired password reset OTP.";
        }

        // Validate new password
        String passwordError = validatePassword(newPassword);

        if (passwordError != null) {
            return passwordError;
        }

        // Hash and update new password
        user.setPassword(passwordEncoder.encode(newPassword));

        // Clear reset OTP after successful reset
        user.setPasswordResetOtp(null);
        user.setPasswordResetOtpExpiry(null);

        userRepository.save(user);

        return "Password reset successfully.";
    }



    @Override
    public String verifyMfa(String email, String otp) {

        Optional<UserEntity> userOptional =
                userRepository.findByEmail(email);

        // Email doesn't exist
        if (userOptional.isEmpty()) {
            return "Email not found";
        }

        UserEntity user = userOptional.get();

        // MFA OTP doesn't exist
        if (user.getMfaOtp() == null) {
            return "MFA OTP not found. Please login again.";
        }

        // MFA OTP expired
        if (user.getMfaOtpExpiry() == null ||
                LocalDateTime.now().isAfter(user.getMfaOtpExpiry())) {

            return "MFA OTP expired. Please login again.";
        }

        // Wrong OTP
        if (!user.getMfaOtp().equals(otp)) {
            return "Invalid MFA OTP";
        }

        // Clear OTP after successful verification
        user.setMfaOtp(null);
        user.setMfaOtpExpiry(null);

        userRepository.save(user);

        // Generate JWT only after MFA verification
        String token = jwtUtil.generateToken(user.getEmail());

        return token;
    }

    private String validatePassword(String password) {

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

        return null;
    }
}
