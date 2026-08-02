package com.asrevo.cvhome.checkout.repositories.order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.model.order.OrderCriteria;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    String ID_FIELD = "id";

    String TELEPHONE_FIELD = "telephone";

    @Query("""
            select o from Order o
            join fetch o.orderProducts op
            left join fetch o.delivery od
            left join fetch o.billing ob
            join fetch o.orderTotal ot left
            join fetch o.orderHistory oh left
            join fetch op.downloads opd left
            join fetch op.orderAttributes opa
            left join fetch op.prices opp where o.id = ?1 and o.storeMerchantId = ?2""")
    Order findOne(Long id, StoreMerchantId storeMerchantId);

    @Query("""
            select new com.asrevo.cvhome.commons.domain.StatisticEntry(
                        cast(cast(date_trunc('day',o.datePurchased) as date ) as string ) ,cast(o.status as string) ,count(o.id) )
            from Order o
            where o.storeMerchantId=:storeId
            and o.datePurchased between :from and :to
            group by date_trunc('day',o.datePurchased),o.status
            """)
    List<StatisticEntry> orderStatistic(@Param("from") Date from, @Param("to") Date to,
                                        @Param("storeId") StoreMerchantId storeMerchantId);

    @Query("""
            select new com.asrevo.cvhome.commons.domain.StatisticEntry(null ,CAST(o.billing.country as string ) ,count(o.id))
            from Order o
            where o.storeMerchantId=:storeId
            and o.datePurchased between :from and :to
            group by o.billing.country
            """)
    List<StatisticEntry> customerStatistic(@Param("from") Date from, @Param("to") Date to,
                                           @Param("storeId") StoreMerchantId storeMerchantId);

    default Page<Order> listOrders(StoreMerchantId store, OrderCriteria criteria, Pageable pageable) {
        return findAll((root, query, cb) -> {
            List<Predicate> predicates = buildPredicates(root, query, cb, store, criteria);
            applyOrdering(query, cb, root, criteria);
            applyDistinctFetch(root, query);
            return cb.and(predicates);
        }, pageable);
    }

    private List<Predicate> buildPredicates(Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb,
            StoreMerchantId store, OrderCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();

        if (store != null) {
            predicates.add(cb.equal(root.get("storeMerchantId"), store));
        }

        if (criteria.getCustomerId() != null) {
            predicates.add(cb.equal(root.get("customerId"), criteria.getCustomerId()));
        }

        Path<Object> billing = root.get("billing");
        addNameLikePredicate(predicates, cb, billing, criteria);
        addEmailLikePredicate(predicates, cb, root, criteria);
        addIdLikePredicate(predicates, cb, root, criteria);
        addPhoneLikePredicate(predicates, cb, root, billing, criteria);

        if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
            predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
        }

        return predicates;
    }

    private void addNameLikePredicate(List<Predicate> predicates, CriteriaBuilder cb, Path<Object> billing,
            OrderCriteria criteria) {
        if (criteria.getCustomerName() == null || criteria.getCustomerName().isEmpty()) {
            return;
        }
        String likeValue = Constants.PERCENT_SYMBOL + criteria.getCustomerName() + Constants.PERCENT_SYMBOL;

        Predicate firstName = cb.like(billing.get("firstName"), likeValue);
        Predicate lastName = cb.like(billing.get("lastName"), likeValue);

        predicates.add(cb.or(firstName, lastName));
    }

    private void addEmailLikePredicate(List<Predicate> predicates, CriteriaBuilder cb, Root<Order> root,
            OrderCriteria criteria) {
        if (criteria.getEmail() == null || criteria.getEmail().isEmpty()) {
            return;
        }
        predicates.add(cb.like(root.get("customerEmailAddress"),
                Constants.PERCENT_SYMBOL + criteria.getEmail() + Constants.PERCENT_SYMBOL));
    }

    private void addIdLikePredicate(List<Predicate> predicates, CriteriaBuilder cb, Root<Order> root,
            OrderCriteria criteria) {
        if (criteria.getId() == null) {
            return;
        }
        predicates.add(cb.like(cb.function("str", String.class, root.get(ID_FIELD)),
                Constants.PERCENT_SYMBOL + criteria.getId() + Constants.PERCENT_SYMBOL));
    }

    private void addPhoneLikePredicate(List<Predicate> predicates, CriteriaBuilder cb, Root<Order> root,
            Path<Object> billing, OrderCriteria criteria) {
        if (criteria.getCustomerPhone() == null || criteria.getCustomerPhone().isEmpty()) {
            return;
        }
        String likeValue = Constants.PERCENT_SYMBOL + criteria.getCustomerPhone() + Constants.PERCENT_SYMBOL;

        Predicate billingPhone = cb.like(billing.get(TELEPHONE_FIELD), likeValue);
        Predicate deliveryPhone = cb.like(root.get("delivery").get(TELEPHONE_FIELD), likeValue);

        predicates.add(cb.or(billingPhone, deliveryPhone));
    }

    private void applyOrdering(CriteriaQuery<?> query, CriteriaBuilder cb, Root<Order> root, OrderCriteria criteria) {
        if (criteria.getOrderBy() == null) {
            return;
        }
        if (criteria.getOrderBy().name().equals("ASC")) {
            query.orderBy(cb.asc(root.get(ID_FIELD)));
        } else {
            query.orderBy(cb.desc(root.get(ID_FIELD)));
        }
    }

    private void applyDistinctFetch(Root<Order> root, CriteriaQuery<?> query) {
        if (Order.class.equals(query.getResultType())) {
            root.fetch("orderTotal", JoinType.LEFT);
            query.distinct(true);
        }
    }

    Optional<Order> findOrderByShoppingCartCodeAndStoreMerchantId(String shoppingCartCode, StoreMerchantId storeMerchantId);

    @Query("select o from Order o where o.status = 'PENDING_PAYMENT' and o.paymentType = :paymentType and o.datePurchased < :cutoff")
    List<Order> findExpiredOrders(@Param("paymentType") PaymentType paymentType, @Param("cutoff") Instant cutoff);
}