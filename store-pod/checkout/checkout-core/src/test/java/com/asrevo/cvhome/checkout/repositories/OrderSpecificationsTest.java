package com.asrevo.cvhome.checkout.repositories;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.checkout.domain.OrderRef;
import com.asrevo.cvhome.checkout.entity.Customer;
import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.checkout.entity.Orders;
import com.asrevo.cvhome.checkout.model.customer.CustomerFilter;
import com.asrevo.cvhome.checkout.model.order.OrderFilter;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

/**
 * The store predicate is always there; each filter adds exactly one more, and blank text adds none.
 */
class OrderSpecificationsTest {

    private static final String A_B = "a@b";

    private static final String ADA = "ada";

    private static final String BLANK = " ";

    @SuppressWarnings("unchecked")
    private static <T> Root<T> root() {
        return mock(Root.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS));
    }

    private static CriteriaBuilder builder() {
        return mock(CriteriaBuilder.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS));
    }

    @SuppressWarnings("unchecked")
    private static int predicateCount(CriteriaBuilder cb) {
        ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
        verify(cb).and(captor.capture());
        return captor.getValue().length;
    }

    @Test
    void anEmptyOrderFilterIsJustTheStore() {
        CriteriaBuilder cb = builder();
        Root<Order> root = root();

        OrderSpecifications.orders(Orders.STORE, OrderFilter.none()).toPredicate(root, mock(CriteriaQuery.class), cb);

        assertThat(predicateCount(cb)).isEqualTo(1);
        verify(cb, never()).like(any(), any(String.class));
    }

    @Test
    void everyOrderFilterAddsOnePredicate() {
        CriteriaBuilder cb = builder();
        Root<Order> root = root();

        OrderSpecifications.orders(Orders.STORE, new OrderFilter(ADA, 5L, OrderStatus.CONFIRMED, "+44", A_B, 7L, null))
                .toPredicate(root, mock(CriteriaQuery.class), cb);

        assertThat(predicateCount(cb)).isEqualTo(7);
        verify(cb, times(4)).like(any(), any(String.class));
        verify(cb).or(any(), any());
    }

    @Test
    void theRefFilterIsAnExactMatchOnTheOrderRef() {
        CriteriaBuilder cb = builder();

        OrderSpecifications.orders(Orders.STORE, new OrderFilter(null, null, null, null, null, null, " abc "))
                .toPredicate(root(), mock(CriteriaQuery.class), cb);

        assertThat(predicateCount(cb)).isEqualTo(2);
        verify(cb).equal(any(), eq(OrderRef.of("abc")));
    }

    @Test
    void blankTextFiltersAreIgnored() {
        CriteriaBuilder cb = builder();

        OrderSpecifications.orders(Orders.STORE, new OrderFilter("  ", null, null, "", BLANK, null, BLANK))
                .toPredicate(root(), mock(CriteriaQuery.class), cb);

        assertThat(predicateCount(cb)).isEqualTo(1);
    }

    @Test
    void everyCustomerFilterAddsOnePredicate() {
        CriteriaBuilder cb = builder();
        Root<Customer> root = root();

        OrderSpecifications.customers(Orders.STORE, new CustomerFilter(ADA, "Ada", "Lovelace", A_B, "gb"))
                .toPredicate(root, mock(CriteriaQuery.class), cb);

        assertThat(predicateCount(cb)).isEqualTo(6);
        verify(cb, atLeast(1)).upper(any());
        verify(cb).equal(any(), org.mockito.ArgumentMatchers.eq("GB"));
    }

    @Test
    void anEmptyCustomerFilterIsJustTheStore() {
        CriteriaBuilder cb = builder();

        OrderSpecifications.customers(Orders.STORE, CustomerFilter.none())
                .toPredicate(root(), mock(CriteriaQuery.class), cb);

        assertThat(predicateCount(cb)).isEqualTo(1);
    }
}
