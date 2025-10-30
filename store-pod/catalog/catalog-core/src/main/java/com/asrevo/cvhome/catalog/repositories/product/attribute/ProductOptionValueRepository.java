package com.asrevo.cvhome.catalog.repositories.product.attribute;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, Long> {

	@Query("""
			select p from ProductOptionValue p
			left join fetch p.descriptions pd
			where p.id = ?2  and p.storeMerchantId = ?1""")
	ProductOptionValue findOne(StoreMerchantId storeMerchantId, Long id);

	@Query("""
			select p from ProductOptionValue p
			left join fetch p.descriptions pd
			where p.storeMerchantId = ?1 and p.code = ?2""")
	ProductOptionValue findByCode(StoreMerchantId storeMerchantId, String optionValueCode);

}
