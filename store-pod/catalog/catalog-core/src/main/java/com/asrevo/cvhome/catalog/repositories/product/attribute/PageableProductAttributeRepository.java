package com.asrevo.cvhome.catalog.repositories.product.attribute;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.LanguageCode;

public interface PageableProductAttributeRepository extends PagingAndSortingRepository<Category, Long> {

    @Query(value = """
            select distinct p from ProductAttribute p
            join fetch p.product pr
            left join fetch p.productOption po
            left join fetch p.productOptionValue pov
            left join fetch po.descriptions pod
            left join fetch pov.descriptions povd
            where po.storeMerchantId = ?1
            and pr.id = ?2 and povd.languageCode = ?3""", countQuery = """
            select  count(p)
            from ProductAttribute p
            join p.product pr
            where pr.store = ?1 and pr.id = ?2""")
    Page<ProductAttribute> findByProductId(StoreMerchantId storeMerchantId, Long productId, LanguageCode languageId,
                                           Pageable pageable);

    @Query(value = """
            select distinct p from ProductAttribute p
            join fetch p.product pr
            left join fetch p.productOption po
            left join fetch p.productOptionValue pov
            left join fetch po.descriptions pod
            left join fetch pov.descriptions povd
            where po.storeMerchantId = ?1 and pr.id = ?2""", countQuery = """
            select  count(p)
            from ProductAttribute p
            join p.product pr
            where pr.store = ?1 and pr.id = ?2""")
    Page<ProductAttribute> findByProductId(StoreMerchantId storeMerchantId, Long productId, Pageable pageable);

}
