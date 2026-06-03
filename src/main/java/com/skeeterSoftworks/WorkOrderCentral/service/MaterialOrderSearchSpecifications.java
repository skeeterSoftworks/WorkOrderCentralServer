package com.skeeterSoftworks.WorkOrderCentral.service;

import com.skeeterSoftworks.WorkOrderCentral.domain.objects.Material;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialOrder;
import com.skeeterSoftworks.WorkOrderCentral.domain.objects.MaterialProvider;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class MaterialOrderSearchSpecifications {

    private MaterialOrderSearchSpecifications() {
    }

    public static Specification<MaterialOrder> from(MaterialOrderSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<MaterialOrder, Material> material = root.join("material", JoinType.LEFT);
            Join<MaterialOrder, MaterialProvider> provider = root.join("materialProvider", JoinType.LEFT);

            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }

            Expression<LocalDateTime> createdOrChanged = cb.coalesce(
                    root.get("createdAt"),
                    root.get("lastChanged"));
            if (criteria.getCreatedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        createdOrChanged,
                        criteria.getCreatedFrom().atStartOfDay()));
            }
            if (criteria.getCreatedTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        createdOrChanged,
                        criteria.getCreatedTo().atTime(LocalTime.MAX)));
            }

            if (StringUtils.hasText(criteria.getCode())) {
                String pattern = likePattern(criteria.getCode());
                predicates.add(cb.like(cb.lower(root.get("code")), pattern));
            }

            if (StringUtils.hasText(criteria.getMaterialName())) {
                String pattern = likePattern(criteria.getMaterialName());
                predicates.add(cb.or(
                        cb.like(cb.lower(material.get("name")), pattern),
                        cb.like(cb.lower(material.get("code")), pattern)));
            }

            if (StringUtils.hasText(criteria.getMaterialProviderName())) {
                String pattern = likePattern(criteria.getMaterialProviderName());
                predicates.add(cb.like(cb.lower(provider.get("name")), pattern));
            }

            if (criteria.getQuantity() != null) {
                predicates.add(cb.equal(root.get("quantity"), criteria.getQuantity()));
            }

            if (criteria.getLastChangedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("lastChanged"),
                        criteria.getLastChangedFrom().atStartOfDay()));
            }
            if (criteria.getLastChangedTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("lastChanged"),
                        criteria.getLastChangedTo().atTime(LocalTime.MAX)));
            }

            if (criteria.getCertificatePresent() != null) {
                predicates.add(certificatePresentPredicate(root, cb, criteria.getCertificatePresent()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate certificatePresentPredicate(
            Root<MaterialOrder> root,
            CriteriaBuilder cb,
            boolean present) {
        return cb.equal(root.get("certificatePresent"), present);
    }

    private static String likePattern(String raw) {
        return "%" + raw.trim().toLowerCase() + "%";
    }
}
