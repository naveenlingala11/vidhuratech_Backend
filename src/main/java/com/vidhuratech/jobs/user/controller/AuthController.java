package com.vidhuratech.jobs.user.controller;

import com.vidhuratech.jobs.user.dto.AuthResponse;
import com.vidhuratech.jobs.user.dto.LoginRequest;
import com.vidhuratech.jobs.user.dto.RegisterRequest;
import com.vidhuratech.jobs.user.entity.PasswordResetToken;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.PasswordResetTokenRepository;
import com.vidhuratech.jobs.user.repository.UserRepository;
import com.vidhuratech.jobs.user.service.AuthService;
import com.vidhuratech.jobs.user.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    // ✅ SET PASSWORD
    @PostMapping("/set-password")
    public ResponseEntity<?> setPassword(
            @RequestParam String token,
            @RequestParam String password) {

        PasswordResetToken t = tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (t.isExpired()) {
            throw new RuntimeException("expired");
        }

        if (Boolean.TRUE.equals(t.getUsed())) {
            throw new RuntimeException("used");
        }

        User user = userRepo.findByEmail(t.getEmail())
                .orElseThrow();

        user.setPassword(passwordEncoder.encode(password));
        user.setFirstLogin(false);

        userRepo.save(user);

        t.setUsed(true);
        tokenRepo.save(t);

        return ResponseEntity.ok(Map.of("message", "Password set successfully"));
    }

    // ✅ SEND OTP
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {
        otpService.sendOtp(email);
        return ResponseEntity.ok("OTP sent");
    }

    // ✅ VERIFY OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {

        return ResponseEntity.ok(otpService.verifyOtp(email, otp));
    }

    @PostMapping("/register/init")
    public ResponseEntity<?> initRegister(@RequestBody RegisterRequest req) {

        if (userRepo.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email already registered"));
        }

        otpService.initiateRegistration(req);

        return ResponseEntity.ok(Map.of("message", "OTP sent"));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<AuthResponse> verifyRegister(
            @RequestParam String email,
            @RequestParam String otp) {

        return ResponseEntity.ok(
                otpService.verifyRegistrationOtp(email, otp)
        );
    }

    @PostMapping("/resend-link")
    public ResponseEntity<?> resendLink(@RequestBody Map<String, String> body) {

        String token = body.get("token");

        PasswordResetToken old = tokenRepo.findByToken(token)
                .orElseThrow();

        String email = old.getEmail();

        // create new token
        String newToken = UUID.randomUUID().toString();

        PasswordResetToken t = PasswordResetToken.builder()
                .token(newToken)
                .email(email)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        tokenRepo.save(t);

        // send email again
        // (reuse your email service)

        return ResponseEntity.ok(Map.of("message", "Resent"));
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {

        return tokenRepo.findByToken(token)
                .map(t -> {

                    if (t.isExpired()) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "expired"));
                    }

                    return ResponseEntity.ok(Map.of(
                            "valid", true,
                            "used", Boolean.TRUE.equals(t.getUsed())
                    ));
                })
                .orElse(ResponseEntity.badRequest()
                        .body(Map.of("message", "invalid")));
    }
}