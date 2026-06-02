package com.vidhuratech.jobs.plans.repository;

import com.vidhuratech.jobs.plans.entity.PlanPricingControl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanPricingControlRepository extends JpaRepository<PlanPricingControl, Long> {

    Optional<PlanPricingControl> findByPlanCodeIgnoreCase(String planCode);
}