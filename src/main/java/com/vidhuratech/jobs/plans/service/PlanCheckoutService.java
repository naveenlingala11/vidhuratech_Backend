package com.vidhuratech.jobs.plans.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.vidhuratech.jobs.common.notification.service.ActivityNotificationService;
import com.vidhuratech.jobs.common.service.EmailService;
import com.vidhuratech.jobs.invoice.entity.Invoice;
import com.vidhuratech.jobs.invoice.repository.InvoiceRepository;
import com.vidhuratech.jobs.invoice.service.InvoiceEmailTemplateService;
import com.vidhuratech.jobs.plans.dto.PlanCheckoutRequest;
import com.vidhuratech.jobs.plans.dto.PlanPaymentConfirmRequest;
import com.vidhuratech.jobs.plans.entity.PlanAccessGrant;
import com.vidhuratech.jobs.plans.repository.PlanAccessGrantRepository;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.user.repository.UserRepository;
import com.vidhuratech.jobs.user.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PlanCheckoutService {

    private final PlanCatalogService planCatalogService;
    private final PlanAccessGrantRepository planAccessGrantRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordService passwordService;
    private final EmailService emailService;
    private final InvoiceEmailTemplateService invoiceTemplateService;
    private final ActivityNotificationService notificationService;

    @Value("${RAZORPAY_KEY_ID}")
    private String razorpayKey;

    @Value("${RAZORPAY_KEY_SECRET}")
    private String razorpaySecret;

    public Map<String, Object> initiate(PlanCheckoutRequest request) {
        validateBuyer(request);

        Map<String, Object> plan = planCatalogService.getPlan(request.getPlanCode());
        if (Boolean.FALSE.equals(plan.get("active"))) {
            throw new RuntimeException("Selected plan is not available for purchase");
        }

        Double amount = ((Number) plan.get("amount")).doubleValue();
        String planCode = String.valueOf(plan.get("code"));
        String planName = String.valueOf(plan.get("name"));

        try {
            Invoice invoice = Invoice.builder()
                    .id("INV-" + UUID.randomUUID().toString().substring(0, 8))
                    .leadPhone(cleanPhone(request.getPhone()))
                    .name(request.getName().trim())
                    .email(request.getEmail().trim().toLowerCase())
                    .mobile(cleanPhone(request.getPhone()))
                    .studentAddress(request.getCity())
                    .course(planName + " Plan")
                    .batch("Pricing Plan")
                    .amount(amount)
                    .paidAmount(0.0)
                    .remainingAmount(amount)
                    .paymentStatus("PENDING")
                    .paymentMethod("RAZORPAY")
                    .purchaseType("PRICING_PLAN")
                    .planCode(planCode)
                    .createdAt(LocalDateTime.now())
                    .build();

            invoiceRepository.save(invoice);

            RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);

            JSONObject options = new JSONObject();
            options.put("amount", (int) Math.round(amount * 100));
            options.put("currency", "INR");
            options.put("receipt", invoice.getId());
            options.put("notes", Map.of(
                    "purchaseType", "PRICING_PLAN",
                    "planCode", planCode,
                    "email", invoice.getEmail()
            ));

            Order order = client.orders.create(options);

            invoice.setRazorpayOrderId(order.get("id"));
            invoiceRepository.save(invoice);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("invoiceId", invoice.getId());
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("key", razorpayKey);
            response.put("plan", plan);

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Plan checkout failed: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> confirm(PlanPaymentConfirmRequest request) {
        verifySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (!Objects.equals(invoice.getRazorpayOrderId(), request.getRazorpayOrderId())) {
            throw new RuntimeException("Order mismatch");
        }

        if (Boolean.TRUE.equals(invoice.getPaymentVerified())) {
            return Map.of(
                    "success", true,
                    "message", "Payment already confirmed",
                    "invoiceId", invoice.getId()
            );
        }

        invoice.setPaymentStatus("PAID");
        invoice.setPaidAmount(invoice.getAmount());
        invoice.setRemainingAmount(0.0);
        invoice.setPaymentVerified(true);
        invoice.setVerifiedAt(LocalDateTime.now());
        invoice.setRazorpayPaymentId(request.getRazorpayPaymentId());
        invoice.setRazorpaySignature(request.getRazorpaySignature());

        invoiceRepository.save(invoice);

        User user = createOrReuseStudent(invoice);

        createAccessGrant(invoice, user);

        sendInvoiceEmail(invoice);

        notificationService.notifyStudent(
                user,
                "Plan activated",
                "Your " + invoice.getCourse() + " access is active. Invoice: " + invoice.getId(),
                "PLAN_ACTIVATED",
                "/login?email=" + invoice.getEmail() + "&redirect=/coding-contests"
        );

        notificationService.notifyAdmins(
                "Plan payment received",
                invoice.getName() + " purchased " + invoice.getCourse() + " for Rs. " + invoice.getAmount(),
                "PLAN_PAYMENT_SUCCESS",
                "/dashboard/admin/invoice"
        );

        return Map.of(
                "success", true,
                "invoiceId", invoice.getId(),
                "planCode", invoice.getPlanCode(),
                "userId", user.getId(),
                "message", "Plan activated successfully"
        );
    }

    private void createAccessGrant(Invoice invoice, User user) {
        if (planAccessGrantRepository.existsByInvoiceId(invoice.getId())) {
            return;
        }

        Map<String, Object> plan = planCatalogService.getPlan(invoice.getPlanCode());

        Integer validityDays = ((Number) plan.get("validityDays")).intValue();

        PlanAccessGrant grant = PlanAccessGrant.builder()
                .userId(user.getId())
                .invoiceId(invoice.getId())
                .planCode(String.valueOf(plan.get("code")))
                .planName(String.valueOf(plan.get("name")))
                .buyerName(invoice.getName())
                .buyerEmail(invoice.getEmail())
                .buyerPhone(invoice.getMobile())
                .amount(invoice.getAmount())
                .status("ACTIVE")
                .accessCourses((Boolean) plan.get("accessCourses"))
                .accessMockTests((Boolean) plan.get("accessMockTests"))
                .accessInterviews((Boolean) plan.get("accessInterviews"))
                .accessNotes((Boolean) plan.get("accessNotes"))
                .accessMaterials((Boolean) plan.get("accessMaterials"))
                .accessVideos((Boolean) plan.get("accessVideos"))
                .accessLiveClasses((Boolean) plan.get("accessLiveClasses"))
                .accessPracticeCompanies((Boolean) plan.get("accessPracticeCompanies"))
                .accessPremiumChallenges((Boolean) plan.get("accessPremiumChallenges"))
                .companyLimit(((Number) plan.get("companyLimit")).intValue())
                .startsAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(validityDays))
                .createdAt(LocalDateTime.now())
                .build();

        planAccessGrantRepository.save(grant);
    }

    private User createOrReuseStudent(Invoice invoice) {
        return userRepository.findByEmail(invoice.getEmail())
                .map(user -> {
                    user.setName(invoice.getName());
                    user.setPhone(invoice.getMobile());
                    user.setActive(true);
                    user.setDeleted(false);
                    user.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setName(invoice.getName());
                    user.setEmail(invoice.getEmail());
                    user.setPhone(invoice.getMobile());
                    user.setPassword(passwordEncoder.encode("Temp@123"));
                    user.setRole(UserRole.STUDENT);
                    user.setActive(true);
                    user.setDeleted(false);
                    user.setFirstLogin(true);
                    user.setCreatedAt(LocalDateTime.now());

                    User saved = userRepository.save(user);
                    passwordService.sendSetupPasswordLink(saved.getEmail());
                    return saved;
                });
    }

    private void sendInvoiceEmail(Invoice invoice) {
        if (Boolean.TRUE.equals(invoice.getEmailSent())) {
            return;
        }

        String html = invoiceTemplateService.buildPremiumInvoiceEmail(invoice);

        emailService.sendHtmlEmail(
                invoice.getEmail(),
                "Payment Successful - Vidhura Tech " + invoice.getCourse(),
                html
        );

        invoice.setEmailSent(true);
        invoiceRepository.save(invoice);
    }

    private void validateBuyer(PlanCheckoutRequest request) {
        if (request == null) {
            throw new RuntimeException("Invalid checkout request");
        }

        if (request.getPlanCode() == null || request.getPlanCode().isBlank()) {
            throw new RuntimeException("Plan is required");
        }

        if (request.getName() == null || request.getName().trim().length() < 3) {
            throw new RuntimeException("Valid name is required");
        }

        if (request.getEmail() == null || !request.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new RuntimeException("Valid email is required");
        }

        String phone = cleanPhone(request.getPhone());

        if (!phone.matches("^[6-9][0-9]{9}$")) {
            throw new RuntimeException("Valid mobile number is required");
        }
    }

    private String cleanPhone(String phone) {
        return String.valueOf(phone == null ? "" : phone).replaceAll("\\D", "");
    }

    private void verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            String generated = hmacSha256(payload, razorpaySecret);

            if (!generated.equals(signature)) {
                throw new RuntimeException("Payment signature mismatch");
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
}