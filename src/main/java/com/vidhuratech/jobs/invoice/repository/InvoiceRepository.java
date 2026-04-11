package com.vidhuratech.jobs.invoice.repository;

import com.vidhuratech.jobs.invoice.dto.CourseRevenueDto;
import com.vidhuratech.jobs.invoice.dto.PaymentMethodStatsDto;
import com.vidhuratech.jobs.invoice.dto.RevenueSummaryDto;
import com.vidhuratech.jobs.invoice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface InvoiceRepository extends JpaRepository<Invoice,String>, JpaSpecificationExecutor<Invoice> {

    List<Invoice> findByLeadPhone(String phone);

    Page<Invoice> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
    SELECT new com.vidhuratech.jobs.invoice.dto.RevenueSummaryDto(
        COALESCE(SUM(i.amount),0),
        COALESCE(SUM(CASE WHEN LOWER(i.paymentStatus)='paid' THEN i.amount ELSE 0 END),0),
        COALESCE(SUM(CASE WHEN LOWER(i.paymentStatus)='pending' THEN i.amount ELSE 0 END),0),
        COALESCE(SUM(CASE WHEN LOWER(i.paymentStatus)='partial' THEN i.amount ELSE 0 END),0),
        COUNT(i)
    )
    FROM Invoice i
    """)
    RevenueSummaryDto getRevenueSummary();

    @Query("""
    SELECT new com.vidhuratech.jobs.invoice.dto.CourseRevenueDto(
        i.course,
        SUM(i.amount),
        COUNT(i)
    )
    FROM Invoice i
    GROUP BY i.course
    ORDER BY SUM(i.amount) DESC
    """)
    List<CourseRevenueDto> getCourseRevenueBreakdown();

    @Query("""
    SELECT new com.vidhuratech.jobs.invoice.dto.PaymentMethodStatsDto(
        i.paymentMethod,
        COUNT(i),
        SUM(i.amount)
    )
    FROM Invoice i
    GROUP BY i.paymentMethod
    """)
    List<PaymentMethodStatsDto> getPaymentMethodStats();

    List<Invoice> findTop5ByOrderByAmountDesc();
}