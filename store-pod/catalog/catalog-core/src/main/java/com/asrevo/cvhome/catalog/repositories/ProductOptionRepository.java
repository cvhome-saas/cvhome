package com.asrevo.cvhome.catalog.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.catalog.entity.ProductOption;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    @Query("select o from ProductOption o left join fetch o.values where o.storeMerchantId = ?1 and o.id = ?2")
    Optional<ProductOption> findByStoreAndId(StoreMerchantId store, Long id);

    @Query("select o from ProductOption o left join fetch o.values where o.storeMerchantId = ?1 and o.code = ?2")
    Optional<ProductOption> findByStoreAndCode(StoreMerchantId store, String code);

    boolean existsByStoreMerchantIdAndCode(StoreMerchantId store, String code);

    Page<ProductOption> findByStoreMerchantId(StoreMerchantId store, Pageable pageable);

    /**
     * The delete guards: an option still assigned to a product, or with a value chosen by any variant, must not
     * disappear from under them.
     */
    @Query("select count(a) > 0 from ProductOptionAssignment a where a.option.id = ?1")
    boolean isAssignedToProducts(Long optionId);

    @Query("select count(x) > 0 from ProductVariantOptionValue x where x.optionValue.option.id = ?1")
    boolean isUsedByVariants(Long optionId);
}
