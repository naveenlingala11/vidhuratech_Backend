package com.vidhuratech.jobs.checkout.controller;

import com.razorpay.Utils;
import com.vidhuratech.jobs.checkout.dto.CheckoutRequest;
import com.vidhuratech.jobs.checkout.service.CheckoutService;
import com.vidhuratech.jobs.checkout.service.RazorpayService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final RazorpayService razorpayService;

    // ✅ Correct secret for PAYMENT verification
    @Value("${RAZORPAY_KEY_SECRET}")
    private String razorpaySecret;

    // ✅ Webhook secret (separate)
    @Value("${RAZORPAY_WEBHOOK_SECRET}")
    private String webhookSecret;

    // ✅ INITIATE
    @PostMapping("/initiate")
    public Map<String, Object> initiate(@RequestBody CheckoutRequest request) {
        return checkoutService.initiateCheckout(request);
    }

    // ✅ CONFIRM
    @PostMapping("/confirm")
    public void confirmPayment(
            @RequestParam String invoiceId,
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpayPaymentId,
            @RequestParam String razorpaySignature,
            @RequestParam Long batchId,
            @RequestParam MultipartFile invoicePdf
    ) throws IOException {

        checkoutService.confirmPayment(
                invoiceId,
                razorpayOrderId,
                razorpayPaymentId,
                razorpaySignature,
                batchId,
                invoicePdf
        );
    }

    // ✅ STATUS
    @GetMapping("/status")
    public Map<String, Object> getStatus(
            @RequestParam String phone,
            @RequestParam String invoiceId) {

        return checkoutService.getPaymentStatus(phone, invoiceId);
    }

    // ✅ WEBHOOK
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestBody(required = false) String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        try {

            if (payload == null || signature == null) {
                return ResponseEntity.badRequest().body("Missing data");
            }

            if (!checkoutService.verifyWebhookSignature(payload, signature, webhookSecret)) {
                return ResponseEntity.status(400).body("Invalid signature");
            }

            checkoutService.processWebhook(payload);

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Webhook failed");
        }
    }

    // ✅ APPROVE
    @PostMapping("/approve")
    public void approvePayment(
            @RequestParam String invoiceId,
            @RequestParam MultipartFile invoicePdf) {

        checkoutService.approvePayment(invoiceId, invoicePdf);
    }

    // ✅ CORRECT PAYMENT VERIFICATION
    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> payload) {

        String orderId = payload.get("razorpay_order_id");
        String paymentId = payload.get("razorpay_payment_id");
        String signature = payload.get("razorpay_signature");

        if (orderId == null || paymentId == null || signature == null) {
            return ResponseEntity.badRequest().body("Missing fields");
        }

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            // ✅ Razorpay official verification
            boolean isValid = Utils.verifyPaymentSignature(options, razorpaySecret);

            if (!isValid) {
                return ResponseEntity.status(400).body("Invalid signature");
            }

            return ResponseEntity.ok("Payment verified successfully");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Verification failed");
        }
    }

    // ✅ CREATE ORDER (NOW INSIDE CLASS)
    @PostMapping("/create-order")
    public Map<String, Object> createOrder(@RequestParam Double amount) {
        return razorpayService.createOrder(amount);
    }
}