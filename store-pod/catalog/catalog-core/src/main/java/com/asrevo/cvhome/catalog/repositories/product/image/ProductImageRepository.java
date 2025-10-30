package com.asrevo.cvhome.catalog.repositories.product.image;

import com.asrevo.cvhome.catalog.entity.product.image.ProductImage;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

	@Query("""
			select p from ProductImage p left join fetch p.descriptions pd inner join fetch
			 p.product pp  where p.id = ?1""")
	ProductImage findOne(Long id);

	@Query("""
			select p from ProductImage p left join fetch p.descriptions pd inner join fetch
			 p.product pp where pp.id = ?2 and
			 pp.store = ?3 and p.id = ?1""")
	ProductImage finById(Long imageId, Long productId, StoreMerchantId storeMerchantId);

}
