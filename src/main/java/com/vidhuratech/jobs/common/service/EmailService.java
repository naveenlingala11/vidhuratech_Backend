package com.vidhuratech.jobs.common.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendHtmlEmail(
            String to,
            String subject,
            String htmlContent) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("support@vidhuratech.com");

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send HTML email to {}", to, e);
        }
    }

    @Async
    public void sendHtmlEmailWithAttachment(
            String to,
            String subject,
            String htmlContent,
            byte[] attachmentBytes,
            String fileName) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("support@vidhuratech.com");

            // 🔥 ALWAYS TRY
            helper.addAttachment(fileName, new ByteArrayResource(attachmentBytes));

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Attachment failed, fallback to normal email", e);

            // ✅ fallback
            sendHtmlEmail(to, subject,
                    htmlContent + "<br><br>⚠️ Attachment failed. Contact support.");
        }
    }

    @Async
    public void sendOtpEmailWithLogo(
            String to,
            String subject,
            String htmlContent
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("support@vidhuratech.com");

            ClassPathResource image =
                    new ClassPathResource("static/VidhuraTechLogo.png");

            helper.addInline("logoImage", image);

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Email send failed", e);
        }
    }
}