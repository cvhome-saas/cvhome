package com.asrevo.cvhome.catalog.repositories.product.group;

import com.asrevo.cvhome.catalog.entity.product.group.ProductGroup;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductGroupRepository extends JpaRepository<ProductGroup, Long> {

	@Query("select p from ProductGroup p " + "left join fetch p.descriptions pd "
			+ "where p.storeMerchantId = ?1 and p.code = ?2")
	Optional<ProductGroup> findByStoreAndCode(StoreMerchantId store, String code);

	@Query("select p from ProductGroup p " + "left join fetch p.descriptions pd "
			+ "where p.storeMerchantId = ?1 and pd.languageCode = ?2")
	Page<ProductGroup> findByStore(StoreMerchantId store, LanguageCode language, Pageable pageable);

	@Query("select p from ProductGroup p " + "left join fetch p.descriptions pd "
			+ "where p.storeMerchantId = ?1 and p.parentProduct.id = ?2")
	List<ProductGroup> findByStoreAndParentProduct(StoreMerchantId store, Long productId);

	@Query("select p from ProductGroup p " + "left join fetch p.descriptions pd "
			+ "where p.storeMerchantId = ?1 and p.parentProduct.id = ?2 and p.code = ?3")
	Optional<ProductGroup> findByStoreAndParentProductAndCode(StoreMerchantId store, Long productId, String code);

}
