package com.vidhuratech.jobs.user.service;

import com.vidhuratech.jobs.common.service.EmailService;
import com.vidhuratech.jobs.user.entity.PasswordResetToken;
import com.vidhuratech.jobs.user.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordResetTokenRepository repo;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendSetupPasswordLink(String email) {

        String token = UUID.randomUUID().toString();

        PasswordResetToken t = PasswordResetToken.builder()
                .token(token)
                .email(email)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        repo.save(t);

        // ✅ NO HARDCODE
        String link = frontendUrl + "/set-password?token=" + token;

        String html = """
        <div style="font-family:Arial;padding:20px">
            <h2>🔐 Set Your Password</h2>

            <p>Welcome to <b>Vidhura Tech</b> 🚀</p>

            <p>Please click below to set your password:</p>

            <a href="%s"
               style="background:#2563eb;color:white;padding:12px 20px;
                      text-decoration:none;border-radius:8px;display:inline-block;">
                Set Password
            </a>

            <p style="margin-top:20px;color:#666;">
                This link will expire in 24 hours.
            </p>
        </div>
        """.formatted(link);

        emailService.sendHtmlEmail(email, "Set Your Password - Vidhura Tech", html);
    }
}