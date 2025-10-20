package com.asrevo.cvhome.order.repositories.customer;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerRepository extends JpaRepository<Customer, Long>, CustomerRepositoryCustom {

	@Query("""
			select c from Customer c
			left join fetch c.attributes ca
			left join fetch ca.customerOption cao
			left join fetch ca.customerOptionValue cav
			left join fetch cao.descriptions caod
			left join fetch cav.descriptions
			where c.id = ?1""")
	Customer findOne(Long id);

	@Query("""
			select c from Customer c
			left join fetch c.attributes ca
			left join fetch ca.customerOption cao
			left join fetch ca.customerOptionValue cav
			left join fetch cao.descriptions caod
			left join fetch cav.descriptions
			left join fetch c.delivery cd
			left join fetch c.billing cb
			where c.nick = ?1 and c.storeMerchantId = ?2""")
	Customer findByNick(String nick, StoreMerchantId storeMerchantId);

}
