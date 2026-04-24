package com.vidhuratech.jobs.checkout.dto;

import com.vidhuratech.jobs.leads.entity.Lead;
import lombok.Data;

@Data
public class CheckoutRequest {

    private Lead lead;

    private Double amount;

    private String paymentMethod; // UPI / CASH / CARD

    private String couponCode;

    private Boolean installmentEnabled;

    private Long batchId;
}
