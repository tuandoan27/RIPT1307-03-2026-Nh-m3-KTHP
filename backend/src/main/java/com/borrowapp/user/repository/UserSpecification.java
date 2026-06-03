package com.borrowapp.user.repository;

import com.borrowapp.common.constants.Role;
import com.borrowapp.user.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    private UserSpecification() {}

    public static Specification<User> filter(String search, Role role, Boolean isLocked) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("isDeleted")));

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate byName = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate byCode = cb.like(cb.lower(root.get("studentCode")), pattern);
                predicates.add(cb.or(byName, byCode));
            }

            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }

            if (isLocked != null) {
                predicates.add(cb.equal(root.get("isLocked"), isLocked));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}