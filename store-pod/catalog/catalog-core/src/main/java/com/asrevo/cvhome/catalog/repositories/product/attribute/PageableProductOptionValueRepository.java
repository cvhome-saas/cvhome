package com.asrevo.cvhome.catalog.repositories.product.attribute;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PageableProductOptionValueRepository extends PagingAndSortingRepository<ProductOptionValue, Long> {

	@Query(value = """
			select distinct p from ProductOptionValue p left
			 join fetch p.descriptions pd where p.storeMerchantId = ?1 and (?2 is null or
			 (pd.name like %?2% or p.code like %?2%))""", countQuery = """
			select count(p) from ProductOptionValue p left join
			 p.descriptions pd where p.storeMerchantId = ?1 and (?2 is null or (pd.name like
			 %?2% or p.code like %?2%))""")
	Page<ProductOptionValue> listOptionValues(StoreMerchantId storeMerchantId, String name, Pageable pageable);

}
