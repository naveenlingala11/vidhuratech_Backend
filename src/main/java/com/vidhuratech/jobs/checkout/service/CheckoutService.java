package com.vidhuratech.jobs.checkout.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.vidhuratech.jobs.checkout.dto.CheckoutRequest;
import com.vidhuratech.jobs.common.service.EmailService;
import com.vidhuratech.jobs.invoice.entity.Invoice;
import com.vidhuratech.jobs.invoice.repository.InvoiceRepository;
import com.vidhuratech.jobs.invoice.service.InvoiceEmailTemplateService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckoutService {

    private final LeadRepository leadRepo;
    private final InvoiceRepository invoiceRepo;
    private final EmailService emailService;
    private final InvoiceEmailTemplateService templateService;

    @Value("${RAZORPAY_KEY_ID}")
    private String razorpayKey;

    @Value("${RAZORPAY_KEY_SECRET}")
    private String razorpaySecret;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BatchEnrollmentRepository enrollmentRepo;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private PasswordService passwordService;

    // ================= INITIATE =================
    public Map<String, Object> initiateCheckout(CheckoutRequest request) {

        try {
            log.error("KEY: " + razorpayKey);
            log.error("SECRET: " + razorpaySecret);
            if (request.getAmount() == null || request.getAmount() * 100 < 100) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Minimum amount is ₹1"
                );            }
            Lead lead = request.getLead();
            lead.setSource("PURCHASE");
            lead.setStatus("Payment Pending");

            Lead savedLead = leadRepo.save(lead);

            // 🔥 CREATE INVOICE
            Invoice invoice = Invoice.builder()
                    .id("INV-" + UUID.randomUUID().toString().substring(0, 8))
                    .leadPhone(savedLead.getPhone())
                    .name(savedLead.getName())
                    .email(savedLead.getEmail())
                    .mobile(savedLead.getPhone())
                    .course(savedLead.getCourse())
                    .batch(savedLead.getBatch())
                    .batchId(request.getBatchId())
                    .amount(request.getAmount())
                    .paidAmount(0.0)
                    .remainingAmount(request.getAmount())
                    .paymentStatus("PENDING")
                    .paymentMethod("RAZORPAY")
                    .createdAt(LocalDateTime.now())
                    .build();

            invoiceRepo.save(invoice);

            // 🔥 CREATE RAZORPAY ORDER HERE
            RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);

            JSONObject options = new JSONObject();
            options.put("amount", (int)(request.getAmount() * 100));
            options.put("currency", "INR");
            options.put("receipt", invoice.getId()); // 🔥 LINK

            Order order = client.orders.create(options);

            // 🔥 SAVE ORDER ID IN INVOICE
            invoice.setRazorpayOrderId(order.get("id"));
            invoiceRepo.save(invoice);

            return Map.of(
                    "invoiceId", invoice.getId(),
                    "orderId", order.get("id"),
                    "amount", order.get("amount"),
                    "currency", order.get("currency"),
                    "key", razorpayKey
            );

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Checkout failed: " + e.getMessage()
            );        }
    }

    public Map<String, Object> getPaymentStatus(String phone, String invoiceId) {

        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (!invoice.getLeadPhone().equals(phone)) {
            throw new RuntimeException("Details mismatch");
        }

        return Map.of(
                "invoiceId", invoice.getId(),
                "status", invoice.getPaymentStatus(),
                "amount", invoice.getAmount(),
                "course", invoice.getCourse(),
                "batch", invoice.getBatch()
        );
    }

    // ================= CONFIRM =================
    public void confirmPayment(
            String invoiceId,
            String orderId,
            String paymentId,
            String signature,
            Long batchId,
            MultipartFile invoicePdf) throws IOException {
        log.info("Confirm payment called for invoice: {}", invoiceId);

        verifySignature(orderId, paymentId, signature);

        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        invoice.setPaymentStatus("PAID");
        invoice.setPaidAmount(invoice.getAmount());
        invoice.setRemainingAmount(0.0);
        invoice.setPaymentVerified(true);
        invoice.setVerifiedAt(LocalDateTime.now());

        invoiceRepo.save(invoice);

        Lead lead = leadRepo.findAllByPhoneOrderByCreatedAtDesc(invoice.getLeadPhone())
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        lead.setStatus("Joined");
        leadRepo.save(lead);

        // ✅ SEND EMAIL WITH FRONTEND PDF
        sendSuccessEmail(invoice, invoicePdf);

        Optional<User> existingUser =
                userRepo.findByEmail(invoice.getEmail());

        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {

            user = new User();
            user.setName(invoice.getName());
            user.setEmail(invoice.getEmail());
            user.setPhone(invoice.getMobile());

            user.setPassword(passwordEncoder.encode("Temp@123"));
            user.setRole(UserRole.STUDENT); // ✅ FIXED ENUM

            user = userRepo.save(user); // ✅ IMPORTANT
            passwordService.sendSetupPasswordLink(user.getEmail());
        }

        // =====================================================
        // 🔥 STEP 5: ENROLL STUDENT INTO BATCH
        // =====================================================
        if (invoice.getBatchId() == null) {
            log.error("Batch ID is NULL for invoice: {}", invoice.getId());
            throw new RuntimeException("Batch ID missing. Contact support.");
        }
        Long batchIdFromInvoice = invoice.getBatchId();

        Batch batch = batchRepository.findById(batchIdFromInvoice)
                .orElseThrow(() -> new RuntimeException("Batch not found: " + batchIdFromInvoice));
        System.out.println("INVOICE BATCH ID: " + invoice.getBatchId());
        System.out.println("FRONTEND BATCH ID: " + batchId);

        boolean alreadyEnrolled =
                enrollmentRepo.existsByBatchIdAndStudentId(
                        batch.getId(),
                        user.getId()
                );

        if (!alreadyEnrolled) {

            BatchEnrollment enrollment = new BatchEnrollment();

            enrollment.setBatch(batch);
            enrollment.setStudent(user); // ✅ FIXED
            enrollment.setActive(true);
            enrollment.setEnrolledAt(LocalDateTime.now());

            enrollmentRepo.save(enrollment);
        }

        // =====================================================
        // 🔥 STEP 6: SEND EMAIL WITH PDF
        // =====================================================

        if (invoicePdf != null && !invoicePdf.isEmpty()) {
            sendSuccessEmail(invoice, invoicePdf);
        } else {
            log.warn("Invoice PDF not received for {}", invoice.getId());

            String html = templateService.buildPremiumInvoiceEmail(invoice);

            emailService.sendHtmlEmail(
                    invoice.getEmail(),
                    "🎉 Payment Successful - Vidhura Tech",
                    html
            );
        }
        log.info("Payment confirmed successfully for {}", invoiceId);
        System.out.println("INVOICE ID: " + invoiceId);
        System.out.println("BATCH ID: " + batchId);
        System.out.println("EMAIL: " + invoice.getEmail());
    }
    // ================= EMAIL =================
    private void sendSuccessEmail(Invoice invoice, MultipartFile invoicePdf) {

        try {

            String html = templateService.buildPremiumInvoiceEmail(invoice);

            emailService.sendHtmlEmailWithAttachment(
                    invoice.getEmail(),
                    "🎉 Payment Successful - Vidhura Tech",
                    html,
                    invoicePdf.getBytes(),
                    "Invoice_" + invoice.getId() + ".pdf"
            );

            log.info("Mail sent to {}", invoice.getEmail());

        } catch (Exception e) {
            log.error("Email failed", e);
            throw new RuntimeException("Email failed", e);
        }
    }

    // ================= SIGNATURE =================
    private void verifySignature(String orderId, String paymentId, String signature) {

        try {
            String payload = orderId + "|" + paymentId;

            String generated = hmacSha256(payload, razorpaySecret);

            if (!generated.equals(signature)) {
                throw new RuntimeException(
                        "Signature mismatch\nExpected: " + generated +
                                "\nActual: " + signature
                );
            }

        } catch (Exception e) {
            throw new RuntimeException("Signature verification failed", e);
        }
    }

    private String hmacSha256(String data, String key) throws Exception {

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));

        byte[] raw = mac.doFinal(data.getBytes());

        StringBuilder hex = new StringBuilder(2 * raw.length);

        for (byte b : raw) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) hex.append('0');
            hex.append(s);
        }

        return hex.toString();
    }

    // ================= WEBHOOK =================
    public boolean verifyWebhookSignature(String payload, String signature, String secret) {

        try {
            String generated = hmacSha256(payload, secret);
            return generated.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    public void processWebhook(String payload) {

        JSONObject json = new JSONObject(payload);

        if (!"payment.captured".equals(json.getString("event"))) return;

        JSONObject payment = json
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String orderId = payment.getString("order_id");

        // ✅ FIXED: find by field
        Invoice invoice = invoiceRepo.findAll()
                .stream()
                .filter(i -> orderId.equals(i.getRazorpayOrderId()))
                .findFirst()
                .orElseThrow();

// 🔥 webhook lo PDF undadu → so only DB update
        invoice.setPaymentStatus("PAID");
        invoice.setPaidAmount(invoice.getAmount());
        invoice.setRemainingAmount(0.0);
        invoice.setPaymentVerified(true);
        invoice.setVerifiedAt(LocalDateTime.now());

        invoiceRepo.save(invoice);
    }

    public void approvePayment(String invoiceId, MultipartFile invoicePdf) {

        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        invoice.setPaymentStatus("PAID");
        invoice.setPaidAmount(invoice.getAmount());
        invoice.setRemainingAmount(0.0);
        invoice.setPaymentVerified(true);
        invoice.setVerifiedAt(LocalDateTime.now());

        invoiceRepo.save(invoice);

        // ✅ SEND EMAIL WITH FRONTEND PDF
        sendSuccessEmail(invoice, invoicePdf);
    }

    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            String generated = hmacSha256(payload, razorpaySecret);
            return generated.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}