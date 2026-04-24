package com.vidhuratech.jobs.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vidhuratech.jobs.common.security.JwtUtil;
import com.vidhuratech.jobs.common.service.EmailService;
import com.vidhuratech.jobs.common.service.OTPEmailTemplateService;
import com.vidhuratech.jobs.user.dto.AuthResponse;
import com.vidhuratech.jobs.user.dto.RegisterRequest;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpService {

    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final OTPEmailTemplateService otpEmailTemplateService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final long OTP_TTL = 5; // minutes

    public void initiateRegistration(RegisterRequest req) {

        if (userRepo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        String otp = generateOtp();
        storeOtp(req.getEmail(), otp);
        // store user temporarily
        storePendingUser(req);
        String html = otpEmailTemplateService
                .buildOtpEmailTemplate(req.getName(), otp);

        emailService.sendOtpEmailWithLogo(
                req.getEmail(),
                "Verify your email - OTP",
                html
        );
    }

    public AuthResponse verifyRegistrationOtp(String email, String otp) {

        String storedOtp = getOtp(email);

        if (storedOtp == null) {
            throw new RuntimeException("OTP expired");
        }

        if (!storedOtp.equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        RegisterRequest req;

        try {
            String json = redisTemplate.opsForValue().get("PENDING:" + email);

            if (json == null) {
                throw new RuntimeException("No registration found");
            }

            req = objectMapper.readValue(json, RegisterRequest.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to read user data");
        }

        if (req == null) {
            throw new RuntimeException("No registration found");
        }

        // ✅ CREATE USER NOW
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(UserRole.STUDENT);
        user.setActive(true);

        userRepo.save(user);
        deleteOtp(email);
        redisTemplate.delete("PENDING:" + email);
        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .role(user.getRole())
                .name(user.getName())
                .build();
    }

    // =========================
    // SEND OTP
    // =========================
    public void sendOtp(String email) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = generateOtp();

        storeOtp(email, otp);

        log.info("OTP for {} is {}", email, otp);

        // ✅ EMAIL TEMPLATE (WITH LOGO)
        String html = otpEmailTemplateService.buildOtpEmailTemplate(user.getName(), otp);

        emailService.sendOtpEmailWithLogo(
                email,
                "🔐 Your OTP - Vidhura Tech",
                html
        );
    }

    // =========================
    // VERIFY OTP
    // =========================
    public AuthResponse verifyOtp(String email, String otp) {

        String storedOtp = getOtp(email);

        if (storedOtp == null) {
            throw new RuntimeException("OTP expired");
        }

        if (!storedOtp.equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        deleteOtp(email);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .role(user.getRole())
                .name(user.getName())
                .firstLogin(Boolean.TRUE.equals(user.getFirstLogin()))
                .build();
    }

    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    public void storeOtp(String email, String otp) {
        redisTemplate.opsForValue()
                .set("OTP:" + email, otp, OTP_TTL, TimeUnit.MINUTES);
    }

    public String getOtp(String email) {
        return redisTemplate.opsForValue().get("OTP:" + email);
    }

    public void deleteOtp(String email) {
        redisTemplate.delete("OTP:" + email);
    }

    // =========================
// STORE PENDING USER IN REDIS
// =========================
    public void storePendingUser(RegisterRequest req) {
        try {
            String json = objectMapper.writeValueAsString(req);

            redisTemplate.opsForValue().set(
                    "PENDING:" + req.getEmail(),
                    json,
                    10,
                    TimeUnit.MINUTES
            );

        } catch (Exception e) {
            throw new RuntimeException("Serialization failed");
        }
    }

}