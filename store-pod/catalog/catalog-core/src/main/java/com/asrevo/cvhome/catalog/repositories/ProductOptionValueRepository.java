package com.asrevo.cvhome.catalog.repositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.catalog.entity.ProductOptionValue;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, Long> {

    /**
     * The store's values for a set of ids, with owning option and both translations loaded — used to group a
     * filter's {@code optionValueIds} by option and to label facet buckets. Ids of other stores fall out here,
     * which is what keeps a crafted id list from probing another tenant's vocabulary.
     */
    @Query("""
            select distinct v from ProductOptionValue v
            join fetch v.option o
            left join fetch v.descriptions
            left join fetch o.descriptions
            where v.id in ?1 and o.storeMerchantId = ?2""")
    List<ProductOptionValue> findByIdsInStore(Collection<Long> ids, StoreMerchantId store);
}
