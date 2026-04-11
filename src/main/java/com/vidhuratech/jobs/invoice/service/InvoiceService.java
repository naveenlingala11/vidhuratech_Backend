package com.vidhuratech.jobs.invoice.service;

import com.vidhuratech.jobs.invoice.dto.InvoiceCreateRequest;
import com.vidhuratech.jobs.invoice.dto.MonthlyRevenueDto;
import com.vidhuratech.jobs.invoice.entity.Invoice;
import com.vidhuratech.jobs.invoice.entity.InvoiceInstallment;
import com.vidhuratech.jobs.invoice.repository.InvoiceInstallmentRepository;
import com.vidhuratech.jobs.invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.vidhuratech.jobs.invoice.dto.InvoiceFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository repo;
    private final InvoiceInstallmentRepository installmentRepo;

    public Invoice save(InvoiceCreateRequest request) {

        Invoice invoice = request.getInvoice();

        invoice.setId("INV-" + UUID.randomUUID().toString().substring(0, 8));
        invoice.setCreatedAt(LocalDateTime.now());

        double totalPaid = 0;

        if (request.getInstallments() != null && !request.getInstallments().isEmpty()) {

            for (InvoiceInstallment inst : request.getInstallments()) {

                inst.setId("INS-" + UUID.randomUUID().toString().substring(0, 8));
                inst.setInvoiceId(invoice.getId());

                if ("PAID".equalsIgnoreCase(inst.getStatus())) {
                    inst.setPaidAmount(inst.getAmount());
                } else {
                    inst.setPaidAmount(0.0);
                }

                totalPaid += inst.getPaidAmount();

                installmentRepo.save(inst);
            }

            invoice.setInstallmentEnabled(true);
        }

        return repo.save(invoice);
    }

    public Invoice update(String id, Invoice updated) {

        Invoice existing = repo.findById(id)
                .orElseThrow();

        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setMobile(updated.getMobile());
        existing.setStudentAddress(updated.getStudentAddress());

        existing.setCourse(updated.getCourse());
        existing.setBatch(updated.getBatch());
        existing.setTrainer(updated.getTrainer());

        existing.setAmount(updated.getAmount());
        existing.setDiscount(updated.getDiscount());
        existing.setScholarship(updated.getScholarship());

        existing.setCouponCode(updated.getCouponCode());
        existing.setPaymentMethod(updated.getPaymentMethod());
        existing.setNotes(updated.getNotes());

        return repo.save(existing);
    }

    public Invoice payInstallment(String installmentId) {

        InvoiceInstallment installment = installmentRepo.findById(installmentId)
                .orElseThrow();

        installment.setStatus("PAID");
        installment.setPaidAmount(installment.getAmount());

        installmentRepo.save(installment);

        Invoice invoice = repo.findById(installment.getInvoiceId())
                .orElseThrow();

        List<InvoiceInstallment> allInstallments =
                installmentRepo.findByInvoiceIdOrderByInstallmentNo(invoice.getId());

        double paid = allInstallments.stream()
                .mapToDouble(x -> x.getPaidAmount() == null ? 0 : x.getPaidAmount())
                .sum();

        invoice.setPaidAmount(paid);
        invoice.setRemainingAmount(invoice.getAmount() - paid);

        if (invoice.getRemainingAmount() <= 0) {
            invoice.setPaymentStatus("PAID");
        } else if (paid > 0) {
            invoice.setPaymentStatus("PARTIAL");
        } else {
            invoice.setPaymentStatus("PENDING");
        }

        return repo.save(invoice);
    }

    public Page<Invoice> filterInvoices(InvoiceFilterRequest filter) {

        Pageable pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return repo.findAll((root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getName() != null && !filter.getName().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("name")),
                                "%" + filter.getName().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getCourse() != null && !filter.getCourse().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("course")),
                                "%" + filter.getCourse().toLowerCase() + "%"
                        )
                );
            }

            if (filter.getPaymentStatus() != null &&
                    !filter.getPaymentStatus().isBlank()) {

                predicates.add(
                        cb.equal(
                                root.get("paymentStatus"),
                                filter.getPaymentStatus()
                        )
                );
            }

            if (filter.getMinAmount() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("amount"),
                                filter.getMinAmount()
                        )
                );
            }

            if (filter.getMaxAmount() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("amount"),
                                filter.getMaxAmount()
                        )
                );
            }

            if (filter.getFromDate() != null && !filter.getFromDate().isBlank()) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("createdAt"),
                                LocalDate.parse(filter.getFromDate()).atStartOfDay()
                        )
                );
            }

            if (filter.getToDate() != null && !filter.getToDate().isBlank()) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("createdAt"),
                                LocalDate.parse(filter.getToDate()).atTime(23,59,59)
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));

        }, pageable);
    }

    public List<MonthlyRevenueDto> getMonthlyRevenue() {

        List<Invoice> invoices = repo.findAll();

        Map<String, Double> monthlyMap = invoices.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getCreatedAt().getMonth().name(),
                        TreeMap::new,
                        Collectors.summingDouble(Invoice::getAmount)
                ));

        return monthlyMap.entrySet().stream()
                .map(e -> new MonthlyRevenueDto(
                        e.getKey(),
                        e.getValue()
                ))
                .toList();
    }
}