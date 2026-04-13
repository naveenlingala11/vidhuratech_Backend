package com.vidhuratech.jobs.common.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.*;
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

            helper.addAttachment(
                    fileName,
                    new ByteArrayResource(attachmentBytes)
            );

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send email with attachment to {}", to, e);
        }
    }
}