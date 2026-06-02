package com.vidhuratech.jobs.plans.repository;

import com.vidhuratech.jobs.plans.entity.DiscountControl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountControlRepository extends JpaRepository<DiscountControl, Long> {

    Optional<DiscountControl> findByCodeIgnoreCase(String code);
}