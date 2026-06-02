package com.vidhuratech.jobs.plans.repository;

import com.vidhuratech.jobs.plans.entity.ProjectAccessControl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectAccessControlRepository extends JpaRepository<ProjectAccessControl, Long> {

    Optional<ProjectAccessControl> findByControlKeyIgnoreCase(String controlKey);
}