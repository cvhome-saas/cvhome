package com.asrevo.cvhome.checkout.repositories.order;

import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.checkout.entity.order.Order;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

	@Query("""
			select o from Order o
			join fetch o.orderProducts op
			left join fetch o.delivery od
			left join fetch o.billing ob
			join fetch o.orderTotal ot left
			join fetch o.orderHistory oh left
			join fetch op.downloads opd left
			join fetch op.orderAttributes opa
			left join fetch op.prices opp where o.id = ?1 and o.store = ?2""")
	Order findOne(Long id, StoreMerchantId storeMerchantId);

	@Query("""
			select new com.asrevo.cvhome.commons.domain.StatisticEntry(cast(cast(date_trunc('day',o.datePurchased) as date ) as string ) ,cast(o.status as string) ,count(o.id))
			from Order o
			where o.store=:storeId
			and o.datePurchased between :from and :to
			group by date_trunc('day',o.datePurchased),o.status
			""")
	List<StatisticEntry> orderStatistic(@Param("from") Date from, @Param("to") Date to,
			@Param("storeId") StoreMerchantId storeMerchantId);

	@Query("""
			select new com.asrevo.cvhome.commons.domain.StatisticEntry(null ,CAST(o.billing.country as string ) ,count(o.id))
			from Order o
			where o.store=:storeId
			and o.datePurchased between :from and :to
			group by o.billing.country
			""")
	List<StatisticEntry> customerStatistic(@Param("from") Date from, @Param("to") Date to,
			@Param("storeId") StoreMerchantId storeMerchantId);

}
