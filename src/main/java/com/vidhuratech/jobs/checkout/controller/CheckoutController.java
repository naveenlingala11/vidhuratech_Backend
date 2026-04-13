package com.vidhuratech.jobs.checkout.controller;

import com.vidhuratech.jobs.checkout.dto.CheckoutRequest;
import com.vidhuratech.jobs.checkout.service.CheckoutService;
import com.vidhuratech.jobs.common.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final EmailService emailService;

    @PostMapping("/initiate")
    public Map<String, Object> initiate(
            @RequestBody CheckoutRequest request) {

        return checkoutService.initiateCheckout(request);
    }

    @PostMapping("/confirm")
    public void confirmPayment(
            @RequestParam String invoiceId) {

        checkoutService.confirmPayment(invoiceId);
    }

    @PostMapping("/submit-proof")
    public void submitProof(
            @RequestParam String invoiceId,
            @RequestParam String utrNumber,
            @RequestParam(required = false) MultipartFile screenshot) {

        checkoutService.submitProof(invoiceId, utrNumber, screenshot);
    }

    @PostMapping("/approve")
    public void approvePayment(
            @RequestParam String invoiceId,
            @RequestParam MultipartFile invoicePdf) {

        checkoutService.approvePayment(invoiceId, invoicePdf);
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus(
            @RequestParam String phone,
            @RequestParam String invoiceId) {

        return checkoutService.getPaymentStatus(phone, invoiceId);
    }

    @GetMapping("/test-mail")
    public String testMail() {

        emailService.sendHtmlEmail(
                "yourpersonalmail@gmail.com",
                "SMTP Test",
                "<h1>Zoho SMTP Working</h1>"
        );

        return "MAIL SENT";
    }
}