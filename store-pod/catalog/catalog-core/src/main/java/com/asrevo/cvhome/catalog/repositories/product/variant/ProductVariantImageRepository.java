package com.asrevo.cvhome.catalog.repositories.product.variant;

import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariantImage;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductVariantImageRepository extends JpaRepository<ProductVariantImage, Long> {

	@Query("""
			select p from ProductVariantImage p
			left join fetch p.descriptions pd
			join fetch p.productVariantGroup pg
			join fetch pg.productVariants pi
			join fetch pi.product ppp
			where pg.id = ?1 and ppp.store = ?2""")
	List<ProductVariantImage> finByProductVariantGroup(Long productVariantGroupId, StoreMerchantId storeMerchantId);

}
