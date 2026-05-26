package com.vidhuratech.jobs.user.controller;

import com.vidhuratech.jobs.common.service.EmailService;
import com.vidhuratech.jobs.common.service.OTPEmailTemplateService;
import com.vidhuratech.jobs.user.dto.*;
import com.vidhuratech.jobs.user.entity.PasswordResetToken;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.PasswordResetTokenRepository;
import com.vidhuratech.jobs.user.repository.UserRepository;
import com.vidhuratech.jobs.user.service.AuthService;
import com.vidhuratech.jobs.user.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
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

    private final EmailService emailService;
    private final OTPEmailTemplateService emailTemplateService;

    @Value("${app.frontend-url}")
    private String frontendUrl;


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
        String email = body.get("email");

        PasswordResetToken oldToken = null;

        if (token != null && !token.isBlank()) {
            oldToken = tokenRepo.findByToken(token)
                    .orElse(null);

            if (oldToken == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Invalid or expired reset link"));
            }

            email = oldToken.getEmail();
        }

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email is required"));
        }

        User user = userRepo.findByEmail(email)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email not registered"));
        }

        if (oldToken != null) {
            oldToken.setUsed(true);
            tokenRepo.save(oldToken);
        }

        String newToken = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(newToken)
                .email(user.getEmail())
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        tokenRepo.save(resetToken);

        String resetLink = frontendUrl + "/set-password?token=" + newToken;

        String name = user.getName() != null && !user.getName().isBlank()
                ? user.getName()
                : "Student";

        String html = emailTemplateService.buildPasswordResetTemplate(name, resetLink);

        emailService.sendOtpEmailWithLogo(
                user.getEmail(),
                "Reset Your Vidhura Tech Password",
                html
        );

        return ResponseEntity.ok(Map.of(
                "message", "Password reset link sent"
        ));
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

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> body = new HashMap<>();
        body.put("id", user.getId());
        body.put("name", user.getName() == null ? "" : user.getName());
        body.put("email", user.getEmail() == null ? "" : user.getEmail());
        body.put("phone", user.getPhone() == null ? "" : user.getPhone());
        body.put("role", user.getRole() == null ? "" : user.getRole().name());
        body.put("active", Boolean.TRUE.equals(user.getActive()));
        body.put("firstLogin", Boolean.TRUE.equals(user.getFirstLogin()));

        return ResponseEntity.ok(body);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMe(
            Authentication authentication,
            @RequestBody UpdateProfileRequest req
    ) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(req.getName() == null ? "" : req.getName().trim());
        user.setPhone(req.getPhone() == null ? "" : req.getPhone().trim());
        user.setUpdatedAt(LocalDateTime.now());

        userRepo.save(user);

        Map<String, Object> body = new HashMap<>();
        body.put("id", user.getId());
        body.put("name", user.getName() == null ? "" : user.getName());
        body.put("email", user.getEmail() == null ? "" : user.getEmail());
        body.put("phone", user.getPhone() == null ? "" : user.getPhone());
        body.put("role", user.getRole() == null ? "" : user.getRole().name());
        body.put("active", Boolean.TRUE.equals(user.getActive()));
        body.put("firstLogin", Boolean.TRUE.equals(user.getFirstLogin()));

        return ResponseEntity.ok(body);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest req
    ) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        if (req.getCurrentPassword() == null || req.getCurrentPassword().isBlank()) {
            throw new RuntimeException("CURRENT_PASSWORD_REQUIRED");
        }

        if (req.getNewPassword() == null || req.getNewPassword().isBlank()) {
            throw new RuntimeException("NEW_PASSWORD_REQUIRED");
        }

        if (req.getNewPassword().length() < 6) {
            throw new RuntimeException("PASSWORD_TOO_SHORT");
        }

        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("CURRENT_PASSWORD_INVALID");
        }

        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("PASSWORD_SAME_AS_OLD");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setFirstLogin(false);
        user.setUpdatedAt(LocalDateTime.now());

        userRepo.save(user);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(
            Authentication authentication,
            @RequestBody VerifyPasswordRequest req
    ) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        if (req.getCurrentPassword() == null || req.getCurrentPassword().isBlank()) {
            throw new RuntimeException("CURRENT_PASSWORD_REQUIRED");
        }

        User user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean valid = passwordEncoder.matches(req.getCurrentPassword(), user.getPassword());

        if (!valid) {
            throw new RuntimeException("CURRENT_PASSWORD_INVALID");
        }

        return ResponseEntity.ok(Map.of(
                "valid", true,
                "message", "Current password verified"
        ));
    }

    private Map<String, Object> buildUserProfileResponse(User user) {
        Map<String, Object> body = new HashMap<>();
        body.put("id", user.getId());
        body.put("name", user.getName() == null ? "" : user.getName());
        body.put("email", user.getEmail() == null ? "" : user.getEmail());
        body.put("phone", user.getPhone() == null ? "" : user.getPhone());
        body.put("role", user.getRole() == null ? "" : user.getRole().name());
        body.put("active", Boolean.TRUE.equals(user.getActive()));
        body.put("firstLogin", Boolean.TRUE.equals(user.getFirstLogin()));
        return body;
    }
}