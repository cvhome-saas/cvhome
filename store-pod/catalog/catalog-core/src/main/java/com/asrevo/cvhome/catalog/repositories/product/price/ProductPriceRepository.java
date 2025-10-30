package com.asrevo.cvhome.catalog.repositories.product.price;

import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductPriceRepository extends JpaRepository<ProductPrice, Long> {

	@Query(value = """
			select distinct p from ProductPrice p
			left join fetch p.productAvailability pa
			left join fetch p.descriptions pd
			join fetch pa.product pap
			left join fetch pa.productVariant ppi
			where pap.sku=?1 or ppi.sku=?1 and pa.storeMerchantId=?2""")
	List<ProductPrice> findByProduct(String sku, StoreMerchantId storeMerchantId);

	@Query(value = """
			select distinct p from ProductPrice p
			left join fetch p.productAvailability pa
			left join fetch p.descriptions pd
			join fetch pa.product pap
			left join fetch pa.productVariant ppi
			where (pap.sku=?1 or ppi.sku=?1)
			and p.id=?2 and pa.storeMerchantId=?3""")
	ProductPrice findByProduct(String sku, Long priceId, StoreMerchantId storeMerchantId);

	@Query(value = """
			select distinct p from ProductPrice p
			left join fetch p.productAvailability pa
			left join fetch p.descriptions pd
			join fetch pa.product pap
			left join fetch pa.productVariant ppi
			where (pap.sku=?1 or ppi.sku=?1)
			and pa.id=?2 and pa.storeMerchantId=?3""")
	List<ProductPrice> findByProductInventoty(String sku, Long ProductInventory, StoreMerchantId storeMerchantId);

}
