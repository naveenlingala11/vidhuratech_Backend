package com.vidhuratech.jobs.user.service;

import com.vidhuratech.jobs.common.config.AppConfig;
import com.vidhuratech.jobs.common.service.EmailService;
import com.vidhuratech.jobs.user.dto.CreateEmployeeDTO;
import com.vidhuratech.jobs.user.dto.UserResponse;
import com.vidhuratech.jobs.user.entity.PasswordResetToken;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.repository.PasswordResetTokenRepository;
import com.vidhuratech.jobs.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PasswordResetTokenRepository tokenRepo;
    private final AppConfig appConfig;

    public UserService(
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            PasswordResetTokenRepository tokenRepo, AppConfig appConfig
    ) {
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenRepo = tokenRepo;
        this.appConfig = appConfig;
    }

    public UserResponse createEmployee(CreateEmployeeDTO dto) {

        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());

        user.setActive(true);
        user.setFirstLogin(true);

        // 🔐 temp password
        user.setPassword(passwordEncoder.encode("Temp@123"));

        userRepo.save(user);

        // 🔥 token
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .email(user.getEmail())
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        tokenRepo.save(resetToken);

        // 🔥 link
        String link = appConfig.getFrontendUrl() + "/set-password?token=" + token;

        // 🔥 email
        String html = buildEmployeeWelcomeEmail(user, dto.getRole().name(), link);

        emailService.sendHtmlEmail(
                user.getEmail(),
                "Welcome to VidhuraTech - Set Your Password",
                html
        );

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getActive()
        );
    }

    private String buildEmployeeWelcomeEmail(User user, String role, String link) {

        return """
    <!DOCTYPE html>
    <html>
    <body style="font-family: Arial, sans-serif; background:#f4f6fb; padding:20px;">

      <div style="max-width:600px;margin:auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 8px 24px rgba(0,0,0,0.05);">

        <!-- HEADER -->
            <div style="background:linear-gradient(135deg,#0d223f,#122b4f);
                        padding:20px;text-align:center">

                    <img src="cid:logoImage" alt="Vidhura Tech" style="height:60px;margin-bottom:10px"/>

                <h2 style="color:#ffffff;margin:0;font-weight:700">
                    Vidhura Tech
                </h2>

                <p style="color:#cbd5e1;margin:5px 0 0">
                    Code Your Future 🚀
                </p>
            </div>

        <!-- BODY -->
        <div style="padding:24px; color:#333;">

          <h3>Hello %s 👋</h3>

          <p>Your account has been successfully created in <b>VidhuraTech Platform</b>.</p>

          <p><b>Role Assigned:</b> %s</p>

          <p>You can now access your dashboard and start working with the system.</p>

          <div style="text-align:center;margin:24px 0;">
            <a href="%s"
               style="background:#2563eb;color:white;padding:12px 20px;border-radius:8px;
               text-decoration:none;font-weight:bold;display:inline-block;">
               Set Your Password
            </a>
          </div>

          <p>This link will expire in <b>24 hours</b>.</p>

          <p>If the button doesn't work, copy and paste this link:</p>
          <p style="word-break:break-all;color:#2563eb;">%s</p>

          <hr style="margin:24px 0;"/>

          <p style="font-size:13px;color:#555;">
            🔐 Never share your credentials with anyone.
            If this wasn't you, contact support immediately.
          </p>

          <p style="font-size:12px;color:#888;margin-top:16px;">
            By using our platform, you agree to our
            <a href="https://www.vidhuratech.com/terms">Terms</a> and
            <a href="https://www.vidhuratech.com/privacy">Privacy Policy</a>.
          </p>

        </div>

        <!-- FOOTER -->
        <div style="background:#f1f4ff;padding:16px;text-align:center;font-size:12px;color:#666;">
          © %d VidhuraTech. All rights reserved.<br/>
          support@vidhuratech.com
        </div>

      </div>

    </body>
    </html>
    """.formatted(
                user.getName(),
                role,
                link,
                link,
                java.time.Year.now().getValue()
        );
    }
}