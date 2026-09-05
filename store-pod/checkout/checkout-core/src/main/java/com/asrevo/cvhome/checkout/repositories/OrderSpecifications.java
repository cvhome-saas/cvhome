package com.asrevo.cvhome.checkout.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.asrevo.cvhome.checkout.entity.Customer;
import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.checkout.model.customer.CustomerFilter;
import com.asrevo.cvhome.checkout.model.order.OrderFilter;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * The console's list filters as JPA specifications. The store predicate is always first and never optional.
 */
public final class OrderSpecifications {

    private static final String STORE = "storeMerchantId";

    private static final String BILLING = "billing";

    private static final String FIRST_NAME = "firstName";

    private static final String LAST_NAME = "lastName";

    private OrderSpecifications() {
    }

    public static Specification<Order> orders(StoreMerchantId store, OrderFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get(STORE), store));
            if (filter.id() != null) {
                predicates.add(cb.equal(root.get("id"), filter.id()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("orderStatus"), filter.status()));
            }
            if (filter.customerId() != null) {
                predicates.add(cb.equal(root.get("customerId"), filter.customerId()));
            }
            if (hasText(filter.email())) {
                predicates.add(contains(cb, root.get("customerEmail"), filter.email()));
            }
            if (hasText(filter.phone())) {
                predicates.add(contains(cb, root.get(BILLING).get("telephone"), filter.phone()));
            }
            if (hasText(filter.name())) {
                predicates.add(nameMatches(cb, root.get(BILLING), filter.name()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<Customer> customers(StoreMerchantId store, CustomerFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get(STORE), store));
            if (hasText(filter.email())) {
                predicates.add(contains(cb, root.get("email"), filter.email()));
            }
            if (hasText(filter.firstName())) {
                predicates.add(contains(cb, root.get(FIRST_NAME), filter.firstName()));
            }
            if (hasText(filter.lastName())) {
                predicates.add(contains(cb, root.get(LAST_NAME), filter.lastName()));
            }
            if (hasText(filter.country())) {
                predicates.add(cb.equal(cb.upper(root.get(BILLING).get("country")),
                        filter.country().toUpperCase(Locale.ROOT)));
            }
            if (hasText(filter.name())) {
                predicates.add(nameMatches(cb, root, filter.name()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static Predicate nameMatches(CriteriaBuilder cb, Path<?> owner, String name) {
        return cb.or(contains(cb, owner.get(FIRST_NAME), name), contains(cb, owner.get(LAST_NAME), name));
    }

    private static Predicate contains(CriteriaBuilder cb, Path<?> path, String value) {
        return cb.like(cb.lower(path.as(String.class)), String.format("%%%s%%", value.trim().toLowerCase(Locale.ROOT)));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
