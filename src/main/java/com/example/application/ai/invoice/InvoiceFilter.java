package com.example.application.ai.invoice;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * The criteria of the AI Grid's filter bar; an empty or {@code null} criterion does not constrain.
 *
 * @param supplier part of the supplier's name, any case
 * @param minGross the gross amount at least; gross is computed, not stored, so this one is checked in memory
 */
public record InvoiceFilter(String supplier, Set<InvoiceCategory> categories, Set<InvoiceStatus> statuses,
                            LocalDate from, LocalDate to, BigDecimal minGross) {

    public static InvoiceFilter empty() {
        return new InvoiceFilter(null, Set.of(), Set.of(), null, null, null);
    }

    /** Everything but the gross amount, for the database. */
    Specification<Invoice> specification() {
        Specification<Invoice> spec = (root, query, cb) -> cb.conjunction();
        if (supplier != null && !supplier.isBlank()) {
            var pattern = "%" + supplier.strip().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("supplier")), pattern));
        }
        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("category").in(categories));
        }
        if (statuses != null && !statuses.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("status").in(statuses));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("invoiceDate"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("invoiceDate"), to));
        }
        return spec;
    }

    boolean matchesGross(Invoice invoice) {
        return minGross == null || invoice.getGross().compareTo(minGross) >= 0;
    }
}
