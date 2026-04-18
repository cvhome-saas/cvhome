package com.asrevo.cvhome.checkout.repositories.order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.model.order.OrderCriteria;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

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

    default Page<Order> listOrders(StoreMerchantId store, OrderCriteria criteria) {
        return findAll((root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("storeMerchantId"), store));

            if (criteria.getCustomerId() != null) {
                predicates.add(cb.equal(root.get("customerId"), criteria.getCustomerId()));
            }

            if (criteria.getCustomerName() != null && !criteria.getCustomerName().isEmpty()) {
                String likeValue = "%" + criteria.getCustomerName() + "%";

                Predicate firstName = cb.like(root.get("billing").get("firstName"), likeValue);
                Predicate lastName = cb.like(root.get("billing").get("lastName"), likeValue);

                predicates.add(cb.or(firstName, lastName));
            }

            if (criteria.getEmail() != null && !criteria.getEmail().isEmpty()) {
                predicates.add(cb.like(root.get("customerEmailAddress"), "%" + criteria.getEmail() + "%"));
            }

            if (criteria.getId() != null) {
                predicates.add(cb.like(cb.function("str", String.class, root.get("id")), "%" + criteria.getId() + "%"));
            }

            if (criteria.getCustomerPhone() != null && !criteria.getCustomerPhone().isEmpty()) {
                String likeValue = "%" + criteria.getCustomerPhone() + "%";

                Predicate billingPhone = cb.like(root.get("billing").get("telephone"), likeValue);
                Predicate deliveryPhone = cb.like(root.get("delivery").get("telephone"), likeValue);

                predicates.add(cb.or(billingPhone, deliveryPhone));
            }

            if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }

            if (criteria.getOrderBy() != null) {
                if (criteria.getOrderBy().name().equals("ASC")) {
                    query.orderBy(cb.asc(root.get("id")));
                } else {
                    query.orderBy(cb.desc(root.get("id")));
                }
            }

            if (Order.class.equals(query.getResultType())) {
                root.fetch("orderTotal", JoinType.LEFT);
                query.distinct(true);
            }

            return cb.and(predicates);

        }, criteria.getPageable());
    }

}
