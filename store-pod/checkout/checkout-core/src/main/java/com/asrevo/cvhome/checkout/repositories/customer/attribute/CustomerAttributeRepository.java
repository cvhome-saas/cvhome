package com.asrevo.cvhome.checkout.repositories.customer.attribute;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.checkout.entity.customer.attribute.CustomerAttribute;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerAttributeRepository extends JpaRepository<CustomerAttribute, Long> {

	@Query("""
			select distinct a from CustomerAttribute a join fetch a.customer ac left join fetch
			 a.customerOption aco left join fetch
			 a.customerOptionValue acov left join fetch aco.descriptions acod left join
			 fetch acov.descriptions acovd where aco.storeMerchantId = ?1 and ac.id = ?2""")
	List<CustomerAttribute> findByCustomerId(StoreMerchantId storeMerchantId, Long customerId);

}
