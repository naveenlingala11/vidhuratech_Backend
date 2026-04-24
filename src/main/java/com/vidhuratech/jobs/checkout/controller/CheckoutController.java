package com.vidhuratech.jobs.checkout.controller;

import com.vidhuratech.jobs.checkout.dto.CheckoutRequest;
import com.vidhuratech.jobs.checkout.service.CheckoutService;
import com.vidhuratech.jobs.checkout.service.RazorpayService;
import com.vidhuratech.jobs.common.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static com.google.common.hash.Hashing.hmacSha256;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/initiate")
    public Map<String, Object> initiate(@RequestBody CheckoutRequest request) {
        return checkoutService.initiateCheckout(request);
    }

    @PostMapping("/confirm")
    public void confirmPayment(
            @RequestParam String invoiceId,
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpayPaymentId,
            @RequestParam String razorpaySignature,
            @RequestParam Long batchId, // ✅ ADD THIS
            @RequestParam MultipartFile invoicePdf
    ) throws IOException {

        checkoutService.confirmPayment(
                invoiceId,
                razorpayOrderId,
                razorpayPaymentId,
                razorpaySignature,
                batchId, // ✅ PASS
                invoicePdf
        );
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus(
            @RequestParam String phone,
            @RequestParam String invoiceId) {

        return checkoutService.getPaymentStatus(phone, invoiceId);
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        if (!checkoutService.verifyWebhookSignature(payload, signature, webhookSecret)) {
            return ResponseEntity.status(400).body("Invalid signature");
        }

        checkoutService.processWebhook(payload);

        return ResponseEntity.ok("OK");
    }

    @PostMapping("/approve")
    public void approvePayment(
            @RequestParam String invoiceId,
            @RequestParam MultipartFile invoicePdf) {

        checkoutService.approvePayment(invoiceId, invoicePdf);
    }
}