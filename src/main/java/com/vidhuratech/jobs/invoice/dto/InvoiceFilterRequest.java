package com.vidhuratech.jobs.invoice.dto;

import lombok.Data;

@Data
public class InvoiceFilterRequest {

    private String name;
    private String course;
    private String paymentStatus;

    private Double minAmount;
    private Double maxAmount;

    private String fromDate;
    private String toDate;

    private Integer page = 0;
    private Integer size = 10;
}