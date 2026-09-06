package com.asrevo.cvhome.checkout.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.domain.OrderRef;
import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Every store-facing query takes the store; the two job queries are deliberately store-agnostic — they walk the
 * whole table by time and hand each id back to a per-order transaction.
 */
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByStoreMerchantIdAndId(StoreMerchantId store, Long id);

    /** The order with its lines, for the step runner, which reads them outside a transaction. */
    @Query("select o from Order o left join fetch o.lines where o.id = :id")
    Optional<Order> findWithLinesById(@Param("id") Long id);

    Optional<Order> findByStoreMerchantIdAndOrderRef(StoreMerchantId store, OrderRef orderRef);

    /** The latest order a cart became; a cart handed back after a refusal can become another. */
    Optional<Order> findFirstByStoreMerchantIdAndCartCodeOrderByIdDesc(StoreMerchantId store, CartCode cartCode);

    Optional<Order> findByStoreMerchantIdAndIdAndCustomerId(StoreMerchantId store, Long id, Long customerId);

    Page<Order> findByStoreMerchantIdAndCustomerIdOrderByDatePurchasedDesc(StoreMerchantId store, Long customerId,
                                                                         Pageable pageable);

    @Query("""
            select o.id from Order o
            where o.pendingAction <> com.asrevo.cvhome.checkout.model.order.PendingAction.NONE
              and o.needsAttention = false
              and o.pendingActionUpdatedAt < :staleBefore
            order by o.pendingActionUpdatedAt asc
            """)
    List<Long> findStalePendingActionIds(@Param("staleBefore") Instant staleBefore, Pageable limit);

    @Query("""
            select o.id from Order o
            where o.orderStatus = com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus.PENDING_PAYMENT
              and o.expiresAt is not null and o.expiresAt < :now
            order by o.expiresAt asc
            """)
    List<Long> findExpiredIds(@Param("now") Instant now, Pageable limit);

    @Query("""
            select new com.asrevo.cvhome.commons.domain.StatisticEntry(
                cast(cast(date_trunc('day', o.datePurchased) as date) as string), cast(o.orderStatus as string),
                count(o.id))
            from Order o
            where o.storeMerchantId = :store and o.datePurchased between :from and :to
            group by date_trunc('day', o.datePurchased), o.orderStatus
            """)
    List<StatisticEntry> ordersPerDayAndStatus(@Param("store") StoreMerchantId store, @Param("from") Instant from,
                                               @Param("to") Instant to);

    @Query("""
            select new com.asrevo.cvhome.commons.domain.StatisticEntry(
                null, cast(o.billing.country as string), count(distinct o.customerId))
            from Order o
            where o.storeMerchantId = :store and o.datePurchased between :from and :to
            group by o.billing.country
            """)
    List<StatisticEntry> customersPerCountry(@Param("store") StoreMerchantId store, @Param("from") Instant from,
                                             @Param("to") Instant to);

    @Query("""
            select new com.asrevo.cvhome.commons.domain.StatisticEntry(null, l.sku, sum(l.quantity))
            from OrderLine l join l.order o
            where o.storeMerchantId = :store and o.datePurchased between :from and :to
            group by l.sku
            """)
    List<StatisticEntry> unitsPerSku(@Param("store") StoreMerchantId store, @Param("from") Instant from,
                                     @Param("to") Instant to);
}
