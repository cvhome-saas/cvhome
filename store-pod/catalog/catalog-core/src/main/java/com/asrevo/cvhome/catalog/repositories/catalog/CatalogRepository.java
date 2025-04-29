package com.asrevo.cvhome.catalog.repositories.catalog;

import com.asrevo.cvhome.catalog.entity.catalog.Catalog;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CatalogRepository extends JpaRepository<Catalog, Long> {

    @Query(
            """
                    select c from Catalog c
                    left join fetch c.entry ce
                    left join fetch ce.category cec where c.code=?1 and c.storeMerchantId = ?2""")
    Optional<Catalog> findByCode(String code, StoreMerchantId storeMerchantId);
}
