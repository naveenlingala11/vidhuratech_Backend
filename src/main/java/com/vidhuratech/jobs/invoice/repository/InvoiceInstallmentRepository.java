package com.vidhuratech.jobs.invoice.repository;

import com.vidhuratech.jobs.invoice.entity.InvoiceInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceInstallmentRepository
        extends JpaRepository<InvoiceInstallment, String> {

    List<InvoiceInstallment> findByInvoiceIdOrderByInstallmentNo(String invoiceId);
}