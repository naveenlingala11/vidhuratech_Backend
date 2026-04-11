package com.vidhuratech.jobs.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueSummaryDto {

    private Double totalRevenue;
    private Double paidRevenue;
    private Double pendingRevenue;
    private Double partialRevenue;
    private Long totalInvoices;
}