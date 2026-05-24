package com.vidhuratech.jobs.trainer.repository;

import com.vidhuratech.jobs.trainer.entity.PseudoCodeDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PseudoCodeDraftRepository
        extends JpaRepository<PseudoCodeDraft, Long> {

    Optional<PseudoCodeDraft> findTopByChallengeIdAndStudentIdOrderBySavedAtDesc(
            Long challengeId,
            Long studentId
    );

    Optional<PseudoCodeDraft>
    findTopByChallengeIdAndStudentIdAndLanguageOrderBySavedAtDesc(
            Long challengeId,
            Long studentId,
            String language
    );
}