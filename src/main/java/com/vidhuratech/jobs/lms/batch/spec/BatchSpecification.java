package com.vidhuratech.jobs.lms.batch.spec;

import com.vidhuratech.jobs.lms.batch.entity.Batch;
import com.vidhuratech.jobs.lms.batch.entity.BatchStatus;
import org.springframework.data.jpa.domain.Specification;

public class BatchSpecification {

    public static Specification<Batch> search(
            String keyword,
            String status,
            Long courseId,
            Long trainerId
    ) {
        return (root, query, cb) -> {

            var predicates = cb.conjunction();

            if (keyword != null && !keyword.isBlank()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%")
                );
            }

            if (status != null && !status.isBlank()) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("status"), BatchStatus.valueOf(status))
                );
            }

            if (courseId != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("course").get("id"), courseId)
                );
            }

            if (trainerId != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("trainer").get("id"), trainerId)
                );
            }

            return predicates;
        };
    }
}