package com.vidhuratech.jobs.lms.batch.scheduler;

import com.vidhuratech.jobs.common.service.EmailService;
import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.entity.BatchEnrollment;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClassReminderScheduler {

    private final BatchEnrollmentRepository enrollmentRepo;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 8 * * *") // daily 8:00 AM
    public void sendClassReminders() {
        LocalDate today = LocalDate.now();
        List<BatchEnrollment> enrollments = enrollmentRepo.findAll();

        for (BatchEnrollment e : enrollments) {
            if (e == null || e.getBatch() == null || e.getStudent() == null) continue;
            if (!Boolean.TRUE.equals(e.getActive())) continue;

            Batch b = e.getBatch();
            if (b.getStartDate() == null) continue;

            long days = ChronoUnit.DAYS.between(today, b.getStartDate());
            if (days != 1 && days != 0) continue;

            String subject = days == 1 ? "Reminder: Class starts tomorrow" : "Reminder: Class starts today";
            String body = """
                <div style="font-family:Arial,sans-serif">
                  <h3>%s</h3>
                  <p>Batch: <b>%s</b></p>
                  <p>Time: <b>%s</b></p>
                  <p>Schedule: <b>%s</b></p>
                  <p><a href="%s">Join Zoom</a></p>
                  <p><a href="%s">Join WhatsApp Group</a></p>
                </div>
                """.formatted(
                    subject,
                    b.getName() == null ? "Batch" : b.getName(),
                    b.getZoomTime() == null ? "Will be shared" : b.getZoomTime(),
                    b.getZoomSchedule() == null ? "Will be shared" : b.getZoomSchedule(),
                    b.getZoomJoinLink() == null ? "#" : b.getZoomJoinLink(),
                    b.getWhatsappGroupLink() == null ? "#" : b.getWhatsappGroupLink()
            );

            try {
                emailService.sendHtmlEmail(e.getStudent().getEmail(), subject, body);
            } catch (Exception ex) {
                log.error("Reminder failed for student {}", e.getStudent().getEmail(), ex);
            }
        }
    }
}
