package com.vidhuratech.jobs.invoice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceInstallment {

    @Id
    private String id;

    private String invoiceId;

    private Integer installmentNo;

    private Double amount;

    private Double paidAmount;

    private LocalDate dueDate;

    private String status; // PAID / PENDING / PARTIAL
}
