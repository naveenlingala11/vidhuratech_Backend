package com.vidhuratech.jobs.checkout.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RazorpayService {

    @Value("${RAZORPAY_KEY_ID}")
    private String key;

    @Value("${RAZORPAY_KEY_SECRET}")
    private String secret;

    public Map<String, Object> createOrder(Double amount) {

        if (amount < 1) {
            throw new RuntimeException("Minimum amount is ₹1");
        }

        try {
            RazorpayClient client = new RazorpayClient(key, secret);

            JSONObject options = new JSONObject();
            options.put("amount", (int)(amount * 100)); // paise
            options.put("currency", "INR");
            options.put("receipt", "txn_" + System.currentTimeMillis());

            Order order = client.orders.create(options);

            return Map.of(
                    "orderId", order.get("id"),
                    "amount", order.get("amount"),
                    "currency", order.get("currency"),
                    "key", key
            );

        } catch (Exception e) {
            throw new RuntimeException("Order creation failed");
        }
    }
}