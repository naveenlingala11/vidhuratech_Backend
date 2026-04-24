package com.vidhuratech.jobs.lms.course.specification;

import com.vidhuratech.jobs.lms.course.dto.CourseSearchFilterDTO;
import com.vidhuratech.jobs.lms.course.entity.Course;
import org.springframework.data.jpa.domain.Specification;

public class CourseSpecification {

    public static Specification<Course> withFilters(CourseSearchFilterDTO filter) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                predicates = cb.and(predicates,
                        cb.or(
                                cb.like(cb.lower(root.get("title")), keyword),
                                cb.like(cb.lower(root.get("code")), keyword)
                        ));
            }

            if (filter.getLevel() != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("level"), filter.getLevel()));
            }

            if (filter.getStatus() != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getActive() != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("active"), filter.getActive()));
            }

            return predicates;
        };
    }
}