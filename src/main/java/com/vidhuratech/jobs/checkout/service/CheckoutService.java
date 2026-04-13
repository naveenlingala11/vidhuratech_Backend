package com.vidhuratech.jobs.checkout.service;

import com.vidhuratech.jobs.checkout.config.PaymentConfig;
import com.vidhuratech.jobs.checkout.dto.CheckoutRequest;
import com.vidhuratech.jobs.common.service.EmailService;
import com.vidhuratech.jobs.invoice.entity.Invoice;
import com.vidhuratech.jobs.invoice.repository.InvoiceRepository;
import com.vidhuratech.jobs.invoice.service.InvoiceEmailTemplateService;
import com.vidhuratech.jobs.leads.entity.Lead;
import com.vidhuratech.jobs.leads.repository.LeadRepository;
import com.vidhuratech.jobs.leads.service.LeadAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final LeadRepository leadRepo;
    private final InvoiceRepository invoiceRepo;
    private final LeadAccessService accessService;
    private final PaymentConfig paymentConfig;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;
    private final InvoiceEmailTemplateService invoiceEmailTemplateService;

//    public Map<String, Object> initiateCheckout(CheckoutRequest request) {
//
//        Lead lead = request.getLead();
//
//        lead.setSource("PURCHASE");
//        lead.setStatus("Payment Pending");
//
//        Lead savedLead = leadRepo.save(lead);
//
//        Invoice invoice = Invoice.builder()
//                .id("INV-" + UUID.randomUUID().toString().substring(0, 8))
//                .leadPhone(savedLead.getPhone())
//                .name(savedLead.getName())
//                .email(savedLead.getEmail())
//                .mobile(savedLead.getPhone())
//                .course(savedLead.getCourse())
//                .batch(savedLead.getBatch())
//                .amount(request.getAmount())
//                .paidAmount(0.0)
//                .remainingAmount(request.getAmount())
//                .paymentStatus("PENDING")
//                .paymentMethod(request.getPaymentMethod())
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        invoiceRepo.save(invoice);
//
//        String upiUrl =
//                "upi://pay?pa=" + paymentConfig.getUpiId() +
//                        "&pn=" + paymentConfig.getMerchantName() +
//                        "&am=" + request.getAmount() +
//                        "&cu=INR" +
//                        "&tn=Course Payment" +
//                        "&tr=" + invoice.getId();
//
//        return Map.of(
//                "invoiceId", invoice.getId(),
//                "upiUrl", upiUrl
//        );
//    }

    public void confirmPayment(String invoiceId) {

        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow();

        invoice.setPaymentStatus("PAID");
        invoice.setPaidAmount(invoice.getAmount());
        invoice.setRemainingAmount(0.0);

        invoiceRepo.save(invoice);

        Lead lead = leadRepo.findByPhone(invoice.getLeadPhone())
                .orElseThrow();

        lead.setStatus("Joined");

        leadRepo.save(lead);

        accessService.grantAccess(lead.getPhone());
    }

    public void submitProof(
            String invoiceId,
            String utrNumber,
            MultipartFile screenshot) {

        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow();

        invoice.setUtrNumber(utrNumber);
        invoice.setPaymentStatus("UNDER_REVIEW");

        if (screenshot != null && !screenshot.isEmpty()) {

            String fileUrl = fileStorageService.store(screenshot);

            invoice.setPaymentScreenshotUrl(fileUrl);
        }

        invoiceRepo.save(invoice);
    }

    public void approvePayment(
            String invoiceId,
            MultipartFile invoicePdf) {

        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow();

        invoice.setPaymentVerified(true);
        invoice.setPaymentStatus("PAID");
        invoice.setPaidAmount(invoice.getAmount());
        invoice.setRemainingAmount(0.0);
        invoice.setVerifiedAt(LocalDateTime.now());

        invoiceRepo.save(invoice);

        Lead lead = leadRepo
                .findAllByPhoneOrderByCreatedAtDesc(invoice.getLeadPhone())
                .stream()
                .findFirst()
                .orElseThrow();

        lead.setStatus("Joined");

        leadRepo.save(lead);

        accessService.grantAccess(lead.getPhone());

        try {

            String html =
                    invoiceEmailTemplateService
                            .buildWelcomeInvoiceEmail(invoice);

            emailService.sendHtmlEmailWithAttachment(
                    invoice.getEmail(),
                    "Welcome to Vidhura Tech - Payment Confirmed",
                    html,
                    invoicePdf.getBytes(),
                    invoicePdf.getOriginalFilename()
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> getPaymentStatus(
            String phone,
            String invoiceId) {

        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (!invoice.getLeadPhone().equals(phone)) {
            throw new RuntimeException("Details mismatch");
        }

        Map<String, Object> response = new HashMap<>();

        response.put("invoiceId", invoice.getId());
        response.put("status", invoice.getPaymentStatus());
        response.put("verified", invoice.getPaymentVerified());
        response.put("amount", invoice.getAmount());
        response.put("course", invoice.getCourse());
        response.put("batch", invoice.getBatch());
        response.put("verifiedAt", invoice.getVerifiedAt());

        return response;
    }

    public Map<String, Object> initiateCheckout(CheckoutRequest request) {

        Lead lead = request.getLead();

        lead.setSource("PURCHASE");
        lead.setStatus("Payment Pending");

        Lead savedLead = leadRepo.save(lead);

        // TEMP TEST OVERRIDE
        // Double payableAmount = 1.0; // <-- Testing QR scan purpose
        // Double payableAmount = request.getAmount(); // PROD revert

        Invoice invoice = Invoice.builder()
                .id("INV-" + UUID.randomUUID().toString().substring(0, 8))
                .leadPhone(savedLead.getPhone())
                .name(savedLead.getName())
                .email(savedLead.getEmail())
                .mobile(savedLead.getPhone())
                .course(savedLead.getCourse())
                .batch(savedLead.getBatch())
                .amount(request.getAmount())
//              .amount(payableAmount)
                .paidAmount(0.0)
                .remainingAmount(request.getAmount())
//              .remainingAmount(payableAmount)
                .paymentStatus("PENDING")
                .paymentMethod(request.getPaymentMethod())
                .createdAt(LocalDateTime.now())
                .build();

        invoiceRepo.save(invoice);

        String upiUrl =
                "upi://pay" +
                        "?pa=" + URLEncoder.encode(paymentConfig.getUpiId(), StandardCharsets.UTF_8) +
                        "&pn=" + URLEncoder.encode(paymentConfig.getMerchantName(), StandardCharsets.UTF_8) +
//                      "&am=" + String.format("%.2f", payableAmount) +
                        "&am=" + String.format("%.2f", request.getAmount()) +
                        "&cu=INR" +
                        "&tn=" + URLEncoder.encode("Course Payment", StandardCharsets.UTF_8) +
                        "&tr=" + URLEncoder.encode(invoice.getId(), StandardCharsets.UTF_8);

        return Map.of(
                "invoiceId", invoice.getId(),
                "upiUrl", upiUrl
        );
    }
}
