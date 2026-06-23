package com.vidhuratech.jobs.admission.service;

import com.vidhuratech.jobs.admission.dto.AdmissionResponseDTO;
import com.vidhuratech.jobs.admission.dto.ManualAdmissionRequest;
import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import com.vidhuratech.jobs.invoice.entity.Invoice;
import com.vidhuratech.jobs.invoice.repository.InvoiceRepository;
import com.vidhuratech.jobs.leads.entity.Lead;
import com.vidhuratech.jobs.leads.repository.LeadRepository;
import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.entity.BatchEnrollment;
import com.vidhuratech.jobs.lms.batch.repository.BatchEnrollmentRepository;
import com.vidhuratech.jobs.lms.batch.repository.BatchRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.user.repository.UserRepository;
import com.vidhuratech.jobs.user.service.PasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdmissionService {

    private final UserRepository userRepo;
    private final BatchRepository batchRepo;
    private final BatchEnrollmentRepository enrollmentRepo;
    private final InvoiceRepository invoiceRepo;
    private final LeadRepository leadRepo;
    private final PasswordService passwordService;
    private final PasswordEncoder passwordEncoder;
    private final ActivityNotificationService notificationService;

    @Transactional
    public AdmissionResponseDTO createManualAdmission(
            ManualAdmissionRequest req
    ) {

        log.info("======================================");
        log.info("MANUAL ADMISSION STARTED");
        log.info("Student Email: {}", req.getEmail());
        log.info("Batch ID: {}", req.getBatchId());
        log.info("======================================");

        boolean studentCreated = false;
        boolean existingStudent = false;
        boolean enrollmentCreated = false;
        boolean alreadyEnrolled = false;
        boolean invoiceGenerated = false;
        boolean mailSent = false;

        User user;

        // =====================================================
        // STEP 1: CHECK EXISTING USER
        // =====================================================

        Optional<User> existingUser =
                userRepo.findByEmail(req.getEmail());

        if (existingUser.isPresent()) {

            // =========================================
            // EXISTING USER
            // =========================================

            user = existingUser.get();
            if (user.getRole() == UserRole.USER) {
                user.setRole(UserRole.STUDENT);
                user = userRepo.save(user);
            }

            existingStudent = true;

            log.info("Existing student found: {}", user.getEmail());

            try {

                passwordService.sendSetupPasswordLink(
                        user.getEmail()
                );

                mailSent = true;

                log.info("Password setup mail resent successfully");

            } catch (Exception e) {

                mailSent = false;

                log.error(
                        "Failed to send password setup mail: {}",
                        e.getMessage(),
                        e
                );
            }

        } else {

            // =========================================
            // CREATE NEW USER
            // =========================================

            log.info("Creating new student account");

            User newUser = new User();

            newUser.setName(req.getName());
            newUser.setEmail(req.getEmail());
            newUser.setPhone(req.getPhone());

            newUser.setRole(UserRole.STUDENT);

            // TEMP PASSWORD
            newUser.setPassword(
                    passwordEncoder.encode("Temp@123")
            );

            user = userRepo.save(newUser);

            studentCreated = true;

            log.info("New student created successfully");
            log.info("Student ID: {}", user.getId());

            // =========================================
            // SEND SETUP PASSWORD MAIL
            // =========================================

            try {

                passwordService.sendSetupPasswordLink(
                        user.getEmail()
                );

                mailSent = true;

                log.info("Setup password mail sent successfully");

            } catch (Exception e) {

                mailSent = false;

                log.error(
                        "Failed to send setup password mail: {}",
                        e.getMessage(),
                        e
                );
            }
        }

        // =====================================================
        // STEP 2: FETCH BATCH
        // =====================================================

        Batch batch = batchRepo.findById(req.getBatchId())
                .orElseThrow(() -> {

                    log.error("Batch not found: {}", req.getBatchId());

                    return new RuntimeException("Batch not found");
                });

        log.info("Batch found: {}", batch.getName());

        // =====================================================
        // STEP 3: ENROLLMENT
        // =====================================================

        boolean enrollmentExists =
                enrollmentRepo.existsByBatchIdAndStudentId(
                        batch.getId(),
                        user.getId()
                );

        if (enrollmentExists) {

            alreadyEnrolled = true;

            log.warn("Student already enrolled in batch");

        } else {

            BatchEnrollment enrollment =
                    BatchEnrollment.builder()
                            .batch(batch)
                            .student(user)
                            .active(true)
                            .enrolledAt(LocalDateTime.now())
                            .build();

            enrollmentRepo.save(enrollment);
            notificationService.notifyStudent(
                    user,
                    "Admission created",
                    "You were added to " + batch.getCourse().getTitle() + " - " + batch.getName(),
                    "MANUAL_ADMISSION_CREATED",
                    "/dashboard/student/courses"
            );

            notificationService.notifyBatchTrainer(
                    batch,
                    "New manual admission",
                    user.getName() + " was added to your batch: " + batch.getName(),
                    "STUDENT_JOINED_BATCH",
                    "/dashboard/trainer/students"
            );

            notificationService.notifyAdmins(
                    "Manual admission completed",
                    user.getName() + " was admitted to " + batch.getCourse().getTitle(),
                    "MANUAL_ADMISSION_CREATED",
                    "/dashboard/admin/admissions"
            );

            enrollmentCreated = true;

            log.info("Student enrolled successfully");
        }

        // =====================================================
        // STEP 4: CREATE LEAD ENTRY
        // =====================================================

        try {

            Lead lead = new Lead();

            lead.setName(user.getName());
            lead.setEmail(user.getEmail());
            lead.setPhone(user.getPhone());

            lead.setCourse(batch.getCourse().getTitle());
            lead.setBatch(batch.getName());

            lead.setStatus("Joined");

            lead.setSource("ADMIN_MANUAL_ADMISSION");

            lead.setCreatedAt(LocalDateTime.now());

            leadRepo.save(lead);

            log.info("Lead entry created");

        } catch (Exception e) {

            log.error(
                    "Lead creation failed: {}",
                    e.getMessage(),
                    e
            );
        }

        // =====================================================
        // STEP 5: CREATE INVOICE
        // =====================================================

        Invoice invoice = Invoice.builder()

                .id(
                        "INV-" +
                                UUID.randomUUID()
                                        .toString()
                                        .substring(0, 8)
                )

                .name(user.getName())

                .email(user.getEmail())

                .mobile(user.getPhone())

                .course(batch.getCourse().getTitle())

                .batch(batch.getName())

                .batchId(batch.getId())

                .amount(req.getAmount())

                .paidAmount(
                        "PAID".equalsIgnoreCase(
                                req.getPaymentStatus()
                        )
                                ? req.getAmount()
                                : 0.0
                )

                .remainingAmount(
                        "PAID".equalsIgnoreCase(
                                req.getPaymentStatus()
                        )
                                ? 0.0
                                : req.getAmount()
                )

                .paymentStatus(req.getPaymentStatus())

                .paymentMethod(req.getPaymentMethod())

                .paymentVerified(
                        "PAID".equalsIgnoreCase(
                                req.getPaymentStatus()
                        )
                )

                .verifiedAt(
                        "PAID".equalsIgnoreCase(
                                req.getPaymentStatus()
                        )
                                ? LocalDateTime.now()
                                : null
                )

                .createdAt(LocalDateTime.now())

                .emailSent(false)

                .build();

        invoiceRepo.save(invoice);

        invoiceGenerated = true;

        log.info("Invoice generated successfully");
        log.info("Invoice ID: {}", invoice.getId());

        // =====================================================
        // FINAL STATUS
        // =====================================================

        String nextStep;

        if (mailSent) {

            nextStep =
                    "Student should check email and setup password";

        } else if (existingStudent) {

            nextStep =
                    "Existing student detected. Student can login using existing password";

        } else {

            nextStep =
                    "Mail failed. Use forgot password option manually";
        }

        log.info("======================================");
        log.info("MANUAL ADMISSION COMPLETED");
        log.info("Student Created: {}", studentCreated);
        log.info("Existing Student: {}", existingStudent);
        log.info("Enrollment Created: {}", enrollmentCreated);
        log.info("Already Enrolled: {}", alreadyEnrolled);
        log.info("Invoice Generated: {}", invoiceGenerated);
        log.info("Mail Sent: {}", mailSent);
        log.info("======================================");

        return AdmissionResponseDTO.builder()

                .studentCreated(studentCreated)

                .existingStudent(existingStudent)

                .enrollmentCreated(enrollmentCreated)

                .invoiceGenerated(invoiceGenerated)

                .setupPasswordMailSent(mailSent)

                .setupPasswordStatus(
                        mailSent
                                ? "MAIL_SENT"
                                : "MAIL_FAILED"
                )

                .studentEmail(user.getEmail())

                .temporaryPassword(
                        studentCreated
                                ? "Temp@123"
                                : "Existing Password"
                )

                .nextStep(nextStep)

                .build();
    }
}