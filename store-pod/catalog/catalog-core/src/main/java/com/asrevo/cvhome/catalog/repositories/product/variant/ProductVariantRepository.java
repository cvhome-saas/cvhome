package com.asrevo.cvhome.catalog.repositories.product.variant;

import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

	@Query("""
			select p from ProductVariant p
			join fetch p.product pr
			left join fetch p.variation pv
			left join fetch pv.productOption pvpo
			left join fetch pv.productOptionValue pvpov
			left join fetch pvpo.descriptions pvpod
			left join fetch pvpov.descriptions pvpovd
			left join fetch p.variationValue pvv
			left join fetch pvv.productOption pvvpo
			left join fetch pvv.productOptionValue pvvpov
			left join fetch pvvpo.descriptions povvpod
			where p.id = ?1
			and pv.storeMerchantId = ?2""")
	Optional<ProductVariant> findOne(Long id, StoreMerchantId storeMerchantId);

	@Query("""
			select p from ProductVariant p
			join fetch p.product pr
			left join fetch p.variation pv
			left join fetch pv.productOption pvpo
			left join fetch pv.productOptionValue pvpov
			left join fetch pvpo.descriptions pvpod
			left join fetch pvpov.descriptions pvpovd
			left join fetch p.variationValue pvv
			left join fetch pvv.productOption pvvpo
			left join fetch pvv.productOptionValue pvvpov
			left join fetch pvvpo.descriptions povvpod
			where p.id in (?1)
			and pv.storeMerchantId = ?2""")
	List<ProductVariant> findByIds(List<Long> ids, StoreMerchantId storeMerchantId);

	@Query("""
			select p from ProductVariant p
			join fetch p.product pr
			left join fetch p.variation pv
			left join fetch pv.productOption pvpo
			left join fetch pv.productOptionValue pvpov
			left join fetch pvpo.descriptions pvpod
			left join fetch pvpov.descriptions pvpovd
			left join fetch p.variationValue pvv
			left join fetch pvv.productOption pvvpo
			left join fetch pvv.productOptionValue pvvpov
			left join fetch pvvpo.descriptions povvpod
			where p.id = ?1
			and pr.id = ?2
			and pr.store = ?3""")
	Optional<ProductVariant> findById(Long id, Long productId, StoreMerchantId storeMerchantId);

	@Query(value = """
			select distinct p from ProductVariant as p
			join fetch p.product pr
			left join fetch p.variation pv
			left join fetch pv.productOption pvpo
			left join fetch pv.productOptionValue pvpov
			left join fetch pvpo.descriptions pvpod
			left join fetch pvpov.descriptions povvpovd
			left join fetch p.variationValue pvv
			left join fetch pvv.productOption pvvpo
			left join fetch pvv.productOptionValue pvvpov
			left join fetch pvvpo.descriptions povvpod
			left join fetch p.productVariantGroup pig
			left join fetch pig.images pigi
			left join fetch pigi.descriptions pigid
			where pr.id = ?2
			and pv.storeMerchantId = ?1""")
	List<ProductVariant> findByProductId(StoreMerchantId storeMerchantId, Long productId);

	@Query("""
			select p from ProductVariant p
			join fetch p.product pr
			where p.sku = ?1
			and pr.id = ?2""")
	ProductVariant existsBySkuAndProduct(String sku, Long productId);

}
