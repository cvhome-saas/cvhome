package com.asrevo.cvhome.checkout.repositories.customer;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.checkout.entity.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerRepository extends JpaRepository<Customer, Long>, CustomerRepositoryCustom {

	@Query("""
			select c from Customer c
			where c.id = ?1""")
	Customer findOne(Long id);

	@Query("""
			select c from Customer c
			left join fetch c.delivery cd
			left join fetch c.billing cb
			where c.nick = ?1 and c.storeMerchantId = ?2""")
	Customer findByNick(String nick, StoreMerchantId storeMerchantId);

}
