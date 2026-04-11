package com.vidhuratech.jobs.invoice.dto;

import com.vidhuratech.jobs.invoice.entity.Invoice;
import com.vidhuratech.jobs.invoice.entity.InvoiceInstallment;
import lombok.Data;

import java.util.List;

@Data
public class InvoiceCreateRequest {

    private Invoice invoice;

    private List<InvoiceInstallment> installments;
}