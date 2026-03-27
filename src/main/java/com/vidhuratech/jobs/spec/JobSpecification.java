package com.vidhuratech.jobs.spec;

import com.vidhuratech.jobs.entity.Job;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class JobSpecification {

    public static Specification<Job> filter(
            String keyword,
            List<String> locations,
            List<String> companies,
            List<String> skills,
            String experience,
            Boolean remote,
            String dateFilter
    ) {
        return (root, query, cb) -> {

            Predicate p = cb.conjunction();

            // keyword → title OR description
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";
                p = cb.and(p, cb.or(
                        cb.like(cb.lower(root.get("title")),       like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }

            // locations (LIKE match, OR across list)
            if (locations != null && !locations.isEmpty()) {
                Predicate locPred = cb.disjunction();
                for (String loc : locations) {
                    locPred = cb.or(locPred,
                            cb.like(cb.lower(root.get("location")),
                                    "%" + loc.toLowerCase() + "%"));
                }
                p = cb.and(p, locPred);
            }

            // companies (join → name LIKE)
            if (companies != null && !companies.isEmpty()) {
                Join<Object, Object> cJoin = root.join("company", JoinType.LEFT);

                Predicate cPred = cb.disjunction();

                for (String c : companies) {
                    String like = "%" + c.toLowerCase() + "%";

                    cPred = cb.or(cPred,
                            cb.or(
                                    cb.like(cb.lower(cJoin.get("name")), "%" + c.toLowerCase() + "%"),
                                    cb.like(cb.lower(root.get("description")), "%" + c.toLowerCase() + "%")
                            )
                    );
                }

                p = cb.and(p, cPred);
            }

            // skills (join → exact name IN list)
            if (skills != null && !skills.isEmpty()) {
                Join<Object, Object> sJoin = root.join("skills", JoinType.LEFT);

                Predicate skillPred = cb.disjunction();

                for (String s : skills) {
                    String like = "%" + s.toLowerCase() + "%";

                    skillPred = cb.or(skillPred,
                            cb.like(cb.lower(sJoin.get("name")), like),
                            cb.like(cb.lower(root.get("description")), like) // 🔥 NEW
                    );
                }

                p = cb.and(p, skillPred);
                query.distinct(true);
            }

            // experience (LIKE)
            if (experience != null && !experience.isBlank()) {
                Predicate expPred = cb.disjunction();

                for (String exp : experience.split(",")) {
                    expPred = cb.or(expPred,
                            cb.like(cb.lower(root.get("experience")),
                                    "%" + exp.trim().toLowerCase() + "%")
                    );
                }

                p = cb.and(p, expPred);
            }

            // remote flag
            if (remote != null) {
                p = cb.and(p, cb.equal(root.get("remote"), remote));
            }

            // dateFilter
            if (dateFilter != null) {
                LocalDateTime cutoff = switch (dateFilter) {
                    case "today" -> LocalDateTime.now().minusDays(1);
                    case "week"  -> LocalDateTime.now().minusDays(7);
                    case "month" -> LocalDateTime.now().minusDays(30);
                    default      -> null;
                };
                if (cutoff != null) {
                    p = cb.and(p, cb.greaterThanOrEqualTo(root.get("postedAt"), cutoff));
                }
            }

            return p;
        };
    }
}