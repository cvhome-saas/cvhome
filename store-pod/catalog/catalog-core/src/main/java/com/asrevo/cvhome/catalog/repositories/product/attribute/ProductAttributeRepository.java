package com.asrevo.cvhome.catalog.repositories.product.attribute;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {

    @Query(
            """
                    select p from ProductAttribute p
                    join fetch p.product pr
                    left join fetch p.productOption po
                    left join fetch p.productOptionValue pov
                    left join fetch po.descriptions pod
                    left join fetch pov.descriptions povd
                    where p.id = ?1""")
    ProductAttribute findOne(Long id);

    @Query(
            """
                    select p from ProductAttribute p
                    join fetch p.product pr
                    left join fetch p.productOption po
                    left join fetch p.productOptionValue pov
                    left join fetch po.descriptions pod
                    left join fetch pov.descriptions povd
                    where po.storeMerchantId = ?1 and po.id = ?2""")
    List<ProductAttribute> findByOptionId(StoreMerchantId storeMerchantId, Long id);

    @Query(
            """
                    select distinct p from ProductAttribute p
                    join fetch p.product pr
                    left join fetch p.productOption po
                    left join fetch p.productOptionValue pov
                    left join fetch po.descriptions pod
                    left join fetch pov.descriptions povd
                    where po.storeMerchantId = ?1 and po.id = ?2""")
    List<ProductAttribute> findByOptionValueId(StoreMerchantId storeMerchantId, Long id);

    @Query(
            value =
                    """
                            select distinct p from ProductAttribute p
                            join fetch p.product pr
                            left join fetch pr.categories prc
                            left join fetch p.productOption po
                            left join fetch p.productOptionValue pov
                            left join fetch po.descriptions pod
                            left join fetch pov.descriptions povd
                            where po.storeMerchantId = ?1
                            and prc.id IN (select c.id from Category c where
                             c.lineage like ?2% and povd.languageCode = ?3)""")
    List<ProductAttribute> findOptionsByCategoryLineage(
            StoreMerchantId storeMerchantId, String lineage, LanguageCode languageId);
}
