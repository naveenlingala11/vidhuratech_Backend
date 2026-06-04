package com.vidhuratech.jobs.common.notification.service;

import com.vidhuratech.jobs.common.notification.entity.ActivityNotification;
import com.vidhuratech.jobs.common.notification.repository.ActivityNotificationRepository;
import com.vidhuratech.jobs.common.security.SecurityUtils;
import com.vidhuratech.jobs.common.service.EmailService;
import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.entity.BatchEnrollment;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityNotificationService {

    private final ActivityNotificationRepository repo;
    private final UserRepository userRepo;
    private final SecurityUtils securityUtils;
    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    private boolean canSendActivityNotificationEmail(String email) {
        return email != null && email.trim().toLowerCase().endsWith("@vidhuratech.com");
    }

    public List<Map<String, Object>> myNotifications() {
        User user = getCurrentUser();

        if (!isNotificationsEnabled(user)) {
            return Collections.emptyList();
        }

        return repo.findForUser(user.getId(), user.getRole())
                .stream()
                .map(this::toMap)
                .toList();
    }

    public long unreadCount() {
        User user = getCurrentUser();

        if (!isNotificationsEnabled(user)) {
            return 0;
        }

        return repo.countUnread(user.getId(), user.getRole());
    }

    public Map<String, Object> myPreferences() {
        User user = getCurrentUser();
        return Map.of("notificationsEnabled", isNotificationsEnabled(user));
    }

    public Map<String, Object> updatePreferences(Boolean enabled) {
        User user = getCurrentUser();
        user.setNotificationsEnabled(Boolean.TRUE.equals(enabled));
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);

        return myPreferences();
    }

    public void notifyUser(User user, String title, String message, String type, String link) {
        if (user == null || !isNotificationsEnabled(user)) {
            return;
        }

        ActivityNotification notification = repo.save(ActivityNotification.builder()
                .recipientUser(user)
                .title(title)
                .message(message)
                .activityType(type)
                .link(link)
                .read(false)
                .emailSent(false)
                .createdAt(LocalDateTime.now())
                .build());

        if (!canSendActivityNotificationEmail(user.getEmail())) {
            notification.setEmailSent(false);
            repo.save(notification);
            return;
        }

        try {
            sendEmail(user.getEmail(), title, message, link);
            notification.setEmailSent(true);
            repo.save(notification);
        } catch (Exception e) {
            notification.setEmailSent(false);
            repo.save(notification);
        }
    }

    public void notifyRole(UserRole role, String title, String message, String type, String link) {
        List<User> users = userRepo.findByRoleAndDeletedFalseAndActiveTrue(role);

        for (User user : users) {
            notifyUser(user, title, message, type, link);
        }
    }

    public void notifyAdmins(String title, String message, String type, String link) {
        notifyRole(UserRole.ADMIN, title, message, type, link);
        notifyRole(UserRole.SUPER_ADMIN, title, message, type, link);
    }

    public void notifyTrainer(User trainer, String title, String message, String type, String link) {
        notifyUser(trainer, title, message, type, link);
    }

    public void notifyStudent(User student, String title, String message, String type, String link) {
        notifyUser(student, title, message, type, link);
    }

    public void notifyBatchTrainer(Batch batch, String title, String message, String type, String link) {
        if (batch == null || batch.getTrainer() == null) {
            return;
        }

        notifyUser(batch.getTrainer(), title, message, type, link);
    }

    public void notifyBatchStudents(
            List<BatchEnrollment> enrollments,
            String title,
            String message,
            String type,
            String link
    ) {
        if (enrollments == null) {
            return;
        }

        for (BatchEnrollment enrollment : enrollments) {
            if (enrollment == null || !Boolean.TRUE.equals(enrollment.getActive())) {
                continue;
            }

            notifyStudent(enrollment.getStudent(), title, message, type, link);
        }
    }

    public void markRead(Long id) {
        User user = getCurrentUser();

        ActivityNotification notification = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!canAccessNotification(user, notification)) {
            throw new RuntimeException("Notification not found");
        }

        notification.setRead(true);
        repo.save(notification);
    }

    private User getCurrentUser() {
        Long userId = securityUtils.getCurrentUserId();

        return userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean isNotificationsEnabled(User user) {
        return user == null || !Boolean.FALSE.equals(user.getNotificationsEnabled());
    }

    private boolean canAccessNotification(User user, ActivityNotification notification) {
        if (user == null || notification == null) {
            return false;
        }

        if (notification.getRecipientUser() != null
                && notification.getRecipientUser().getId() != null
                && notification.getRecipientUser().getId().equals(user.getId())) {
            return true;
        }

        return notification.getRecipientRole() != null
                && notification.getRecipientRole().equals(user.getRole());
    }

    private void sendEmail(String to, String title, String message, String link) {
        if (!canSendActivityNotificationEmail(to)) {
            return;
        }

        String safeTitle = escapeHtml(title);
        String safeMessage = escapeHtml(message);
        String actionUrl = escapeHtml(buildFrontendUrl(link));

        String html = """
                <!DOCTYPE html>
                <html>
                <body style="margin:0;padding:0;background:#f4f7fb;font-family:Arial,Helvetica,sans-serif;color:#0f172a;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f7fb;padding:32px 12px;">
                    <tr>
                      <td align="center">
                        <table width="100%%" cellpadding="0" cellspacing="0" style="max-width:660px;background:#ffffff;border-radius:24px;overflow:hidden;border:1px solid #e5e7eb;box-shadow:0 18px 45px rgba(15,23,42,0.08);">
                          
                          <tr>
                            <td style="padding:30px;background:linear-gradient(135deg,#0f172a,#2563eb);color:#ffffff;">
                              <div style="font-size:12px;font-weight:800;letter-spacing:0.12em;text-transform:uppercase;color:#bfdbfe;">
                                Vidhura Tech Notification
                              </div>
                              <h1 style="margin:12px 0 0;font-size:28px;line-height:1.25;font-weight:900;color:#ffffff;">
                                %s
                              </h1>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:30px;">
                              <div style="padding:20px;background:#eff6ff;border:1px solid #bfdbfe;border-radius:18px;margin-bottom:22px;">
                                <p style="margin:0;font-size:16px;line-height:1.7;color:#1e293b;">
                                  %s
                                </p>
                              </div>

                              <table width="100%%" cellpadding="0" cellspacing="0" style="margin:22px 0;">
                                <tr>
                                  <td style="padding:16px;background:#f8fafc;border-radius:16px;border:1px solid #e2e8f0;">
                                    <p style="margin:0 0 6px;font-size:13px;font-weight:800;color:#64748b;">
                                      What should you do next?
                                    </p>
                                    <p style="margin:0;font-size:14px;line-height:1.65;color:#334155;">
                                      Open your dashboard notification bell to view the latest update and complete any required action.
                                    </p>
                                  </td>
                                </tr>
                              </table>

                              <div style="margin-top:28px;text-align:center;">
                                <a href="%s"
                                   style="display:inline-block;background:#2563eb;color:#ffffff;text-decoration:none;padding:14px 24px;border-radius:14px;font-size:14px;font-weight:900;">
                                  Open Dashboard
                                </a>
                              </div>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(safeTitle, safeMessage, actionUrl);

        emailService.sendHtmlEmail(to, title, html);
    }

    private String buildFrontendUrl(String link) {
        String baseUrl = normalizeFrontendUrl(frontendUrl);

        if (link == null || link.isBlank()) {
            return baseUrl + "/dashboard";
        }

        if (link.startsWith("http://") || link.startsWith("https://")) {
            return link;
        }

        if (link.startsWith("/")) {
            return baseUrl + link;
        }

        return baseUrl + "/" + link;
    }

    private String normalizeFrontendUrl(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:4200";
        }

        String trimmed = url.trim();

        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        return trimmed;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private Map<String, Object> toMap(ActivityNotification notification) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", notification.getId());
        map.put("title", notification.getTitle() == null ? "" : notification.getTitle());
        map.put("message", notification.getMessage() == null ? "" : notification.getMessage());
        map.put("activityType", notification.getActivityType() == null ? "" : notification.getActivityType());
        map.put("link", notification.getLink() == null ? "" : notification.getLink());
        map.put("read", Boolean.TRUE.equals(notification.getRead()));
        map.put("createdAt", notification.getCreatedAt());

        return map;
    }

    public void notifyUserInAppOnly(User user, String title, String message, String type, String link) {
        if (user == null || !isNotificationsEnabled(user)) {
            return;
        }

        repo.save(ActivityNotification.builder()
                .recipientUser(user)
                .title(title)
                .message(message)
                .activityType(type)
                .link(link)
                .read(false)
                .emailSent(false)
                .createdAt(LocalDateTime.now())
                .build());
    }

    public void notifyRoleInAppOnly(UserRole role, String title, String message, String type, String link) {
        List<User> users = userRepo.findByRoleAndDeletedFalseAndActiveTrue(role);

        for (User user : users) {
            notifyUserInAppOnly(user, title, message, type, link);
        }
    }

    public void notifyAdminsInAppOnly(String title, String message, String type, String link) {
        notifyRoleInAppOnly(UserRole.ADMIN, title, message, type, link);
        notifyRoleInAppOnly(UserRole.SUPER_ADMIN, title, message, type, link);
    }

    public void notifyBatchStudentsInAppOnly(
            List<BatchEnrollment> enrollments,
            String title,
            String message,
            String type,
            String link
    ) {
        if (enrollments == null) {
            return;
        }

        Set<Long> studentIds = new LinkedHashSet<>();

        for (BatchEnrollment enrollment : enrollments) {
            if (enrollment == null || !Boolean.TRUE.equals(enrollment.getActive())) {
                continue;
            }

            if (enrollment.getStudent() != null && enrollment.getStudent().getId() != null) {
                studentIds.add(enrollment.getStudent().getId());
            }
        }

        if (studentIds.isEmpty()) {
            return;
        }

        List<User> students = userRepo.findAllById(studentIds);

        for (User student : students) {
            notifyUserInAppOnly(student, title, message, type, link);
        }
    }
}