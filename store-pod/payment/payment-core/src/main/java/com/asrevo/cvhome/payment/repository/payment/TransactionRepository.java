package com.asrevo.cvhome.payment.repository.payment;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.util.StringUtils;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.Transaction;
import com.asrevo.cvhome.payment.models.TransactionSearchFilter;
import com.asrevo.cvhome.store.core.constants.Constants;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findTopByStoreMerchantIdAndRequestRefOrderByTransactionDateDesc(StoreMerchantId storeMerchantId, String ref );

    Optional<Transaction> findByStoreMerchantIdAndInternalRef(StoreMerchantId storeMerchantId, String internalRef);

    default Page<Transaction> findAll(StoreMerchantId store, TransactionSearchFilter filter, Pageable pageable) {
        return findAll((Specification<Transaction>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("storeMerchantId"), store));
            if (Objects.nonNull(filter.status())) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (Objects.nonNull(filter.paymentType())) {
                predicates.add(cb.equal(root.get("paymentType"), filter.paymentType()));
            }
            if (StringUtils.hasText(filter.requestRef())) {
                predicates.add(cb.like(root.get("requestRef"), Constants.PERCENT_SYMBOL + filter.requestRef() + Constants.PERCENT_SYMBOL));
            }
            if (StringUtils.hasText(filter.internalRef())) {
                predicates.add(cb.like(root.get("internalRef"), Constants.PERCENT_SYMBOL + filter.internalRef() + Constants.PERCENT_SYMBOL));
            }
            if (Objects.nonNull(filter.transactionDateFrom())) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), filter.transactionDateFrom()));
            }
            if (Objects.nonNull(filter.transactionDateTo())) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), filter.transactionDateTo()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        }, pageable);
    }
}
