package com.borrowapp.activity.repository;

import com.borrowapp.activity.entity.ActivityLog;
import com.borrowapp.common.constants.ActivityLogAction;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogSpecification {

    private ActivityLogSpecification() {}

    public static Specification<ActivityLog> withFilter(
            ActivityLogAction action,
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return (root, query, cb) -> {

            // Fetch user LAZY trong query lấy data, bỏ qua trong count query
            if (!Long.class.equals(query.getResultType())) {
                root.fetch("user", JoinType.LEFT);
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action));
            }

            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }

            if (startDate != null) {
                // createdAt >= startDate 00:00:00
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"), startDate.atStartOfDay()));
            }

            if (endDate != null) {
                // createdAt < endDate+1 00:00:00 (inclusive endDate)
                predicates.add(cb.lessThan(
                        root.get("createdAt"), endDate.plusDays(1).atStartOfDay()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}