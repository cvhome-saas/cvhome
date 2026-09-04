package com.asrevo.cvhome.payment.repository.payment;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.Transaction;
import com.asrevo.cvhome.payment.models.TransactionSearchFilter;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The transaction search filter, built as a JPA {@link Specification} in a default method.
 *
 * <p>
 * The store predicate is the one that matters: it is added unconditionally, before any filter is read, and it is the
 * only thing standing between a console query and another merchant's payment history. Every other predicate is
 * optional, so a filter field left null must add nothing rather than match nothing — a distinction that is invisible
 * until an empty filter returns an empty page.
 * </p>
 *
 * <p>
 * Driven through a mocked {@link CriteriaBuilder} rather than a database, because what is being asserted is which
 * predicates get built, not what Postgres does with them. The round trip through a real schema is
 * {@code TransactionApiIntegrationTest}'s job.
 * </p>
 */
class TransactionRepositoryTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final String STORE_FIELD = "storeMerchantId";
    private static final String REQUEST_REF = "req-1";
    private static final String INTERNAL_REF = "int-1";
    private static final String CONTAINS = "%%%s%%";

    private TransactionRepository repository;
    private CriteriaBuilder builder;
    private Root<Transaction> root;
    private ArgumentCaptor<Specification<Transaction>> captor;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        repository = Mockito.mock(TransactionRepository.class, Mockito.CALLS_REAL_METHODS);
        builder = Mockito.mock(CriteriaBuilder.class, Mockito.RETURNS_DEEP_STUBS);
        root = Mockito.mock(Root.class, Mockito.RETURNS_DEEP_STUBS);
        captor = ArgumentCaptor.forClass(Specification.class);
        when(root.get(anyString())).thenReturn(Mockito.mock(Path.class));
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
    }

    private Specification<Transaction> specificationFor(TransactionSearchFilter filter) {
        repository.findAll(STORE, filter, PageRequest.of(0, 20));
        verify(repository).findAll(captor.capture(), eq(PageRequest.of(0, 20)));
        return captor.getValue();
    }

    private void build(TransactionSearchFilter filter) {
        specificationFor(filter).toPredicate(root, Mockito.mock(CriteriaQuery.class), builder);
    }

    @Test
    void anEmptyFilterStillScopesToTheStore() {
        build(new TransactionSearchFilter(null, null, null, null, null, null));

        verify(builder).equal(any(), eq(STORE));
        verify(root).get(STORE_FIELD);
        Mockito.verify(builder, Mockito.never()).like(any(), anyString());
    }

    @Test
    void aBlankReferenceIsNotAFilterRatherThanAFilterThatMatchesNothing() {
        build(new TransactionSearchFilter(null, null, "  ", "", null, null));

        Mockito.verify(builder, Mockito.never()).like(any(), anyString());
    }

    @Test
    void aStatusAndPaymentTypeBecomeEqualityPredicates() {
        build(new TransactionSearchFilter(PaymentStatus.PAID, PaymentType.STRIPE, null, null, null, null));

        verify(root).get("status");
        verify(root).get("paymentType");
        verify(builder).equal(any(), eq(PaymentStatus.PAID));
        verify(builder).equal(any(), eq(PaymentType.STRIPE));
    }

    @Test
    void bothReferencesBecomeContainsMatchesSoAPartialOrderNumberFinds() {
        build(new TransactionSearchFilter(null, null, REQUEST_REF, INTERNAL_REF, null, null));

        verify(builder).like(any(), eq(CONTAINS.formatted(REQUEST_REF)));
        verify(builder).like(any(), eq(CONTAINS.formatted(INTERNAL_REF)));
    }

    @Test
    void aDateWindowBecomesInclusiveBoundsOnTheTransactionDate() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-02-01T00:00:00Z");

        build(new TransactionSearchFilter(null, null, null, null, from, to));

        verify(builder).greaterThanOrEqualTo(any(), eq(from));
        verify(builder).lessThanOrEqualTo(any(), eq(to));
        verify(root, Mockito.atLeast(2)).get(TransactionRepository.TRANSACTION_DATE_FIELD);
    }

    @Test
    void everyPredicateIsAndedTogether() {
        build(new TransactionSearchFilter(PaymentStatus.PAID, PaymentType.STRIPE, REQUEST_REF, INTERNAL_REF,
                Instant.EPOCH, Instant.EPOCH.plusSeconds(1)));

        ArgumentCaptor<Predicate[]> predicates = ArgumentCaptor.forClass(Predicate[].class);
        verify(builder).and(predicates.capture());
        assertThat(List.of(predicates.getValue())).hasSize(7);
    }
}
