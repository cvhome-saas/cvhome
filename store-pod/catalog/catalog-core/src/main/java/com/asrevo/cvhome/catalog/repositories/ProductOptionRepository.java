package com.asrevo.cvhome.catalog.repositories;

import java.util.Collection;
import java.util.List;
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

    /**
     * The edit guard: which of these value ids some variant still sells by.
     *
     * An update replaces the whole value set, so a value the merchant drops is orphan-removed — and
     * {@code fk_pvov_value} has no {@code ON DELETE}, so dropping one a variant still points at answered a raw
     * FK violation (a 500) where the delete path answers a named 409. Same rule, both doors.
     */
    @Query("select distinct x.optionValue.id from ProductVariantOptionValue x where x.optionValue.id in ?1")
    List<Long> valueIdsUsedByVariants(Collection<Long> valueIds);
}
