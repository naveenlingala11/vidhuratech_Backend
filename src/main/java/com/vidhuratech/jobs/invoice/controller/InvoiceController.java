package com.vidhuratech.jobs.invoice.controller;

import com.vidhuratech.jobs.invoice.dto.*;
import com.vidhuratech.jobs.invoice.entity.Invoice;
import com.vidhuratech.jobs.invoice.entity.InvoiceInstallment;
import com.vidhuratech.jobs.invoice.repository.InvoiceInstallmentRepository;
import com.vidhuratech.jobs.invoice.repository.InvoiceRepository;
import com.vidhuratech.jobs.invoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
@CrossOrigin("*")
public class InvoiceController {

    private final InvoiceService service;
    private final InvoiceRepository repo;
    private final InvoiceInstallmentRepository installmentRepo;

    @PostMapping
    public Invoice create(@RequestBody InvoiceCreateRequest request) {
        return service.save(request);
    }

    @GetMapping("/{id}")
    public Invoice get(@PathVariable String id) {
        return repo.findById(id).orElseThrow();
    }

    @GetMapping("/phone/{phone}")
    public List<Invoice> getByPhone(@PathVariable String phone) {
        return repo.findByLeadPhone(phone);
    }

    @GetMapping
    public List<Invoice> getAll() {
        return repo.findAll();
    }

    @GetMapping("/paged")
    public Page<Invoice> getPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return repo.findAllByOrderByCreatedAtDesc(
                PageRequest.of(page, size)
        );
    }

    @GetMapping("/{invoiceId}/installments")
    public List<InvoiceInstallment> getInstallments(
            @PathVariable String invoiceId) {

        return installmentRepo.findByInvoiceIdOrderByInstallmentNo(invoiceId);
    }

    @PostMapping("/installments/{installmentId}/pay")
    public Invoice payInstallment(
            @PathVariable String installmentId) {

        return service.payInstallment(installmentId);
    }

    @PutMapping("/{id}")
    public Invoice update(
            @PathVariable String id,
            @RequestBody Invoice invoice) {

        return service.update(id, invoice);
    }

    @PostMapping("/search")
    public Page<Invoice> search(
            @RequestBody InvoiceFilterRequest filter) {

        return service.filterInvoices(filter);
    }

    @GetMapping("/analytics/summary")
    public RevenueSummaryDto summary() {
        return repo.getRevenueSummary();
    }

    @GetMapping("/analytics/monthly")
    public List<MonthlyRevenueDto> monthly() {
        return service.getMonthlyRevenue();
    }

    @GetMapping("/analytics/course-breakdown")
    public List<CourseRevenueDto> courseBreakdown() {
        return repo.getCourseRevenueBreakdown();
    }

    @GetMapping("/analytics/payment-methods")
    public List<PaymentMethodStatsDto> paymentMethods() {
        return repo.getPaymentMethodStats();
    }

    @GetMapping("/analytics/top-invoices")
    public List<Invoice> topInvoices() {
        return repo.findTop5ByOrderByAmountDesc();
    }
}