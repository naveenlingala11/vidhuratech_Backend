package com.vidhuratech.jobs.publicpractice.repository;

import com.vidhuratech.jobs.publicpractice.entity.PublicPracticeAccessGrant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublicPracticeAccessGrantRepository
        extends JpaRepository<PublicPracticeAccessGrant, Long> {

    Optional<PublicPracticeAccessGrant> findByAccessTokenAndActiveTrue(String accessToken);
}