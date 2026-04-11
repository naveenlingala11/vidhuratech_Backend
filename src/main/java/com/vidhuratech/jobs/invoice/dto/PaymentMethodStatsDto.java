package com.vidhuratech.jobs.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodStatsDto {

    private String paymentMethod;
    private Long count;
    private Double revenue;
}