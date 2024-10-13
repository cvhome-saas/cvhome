package com.asrevo.cvhome.store.core.repositories.catalog.catalog;

import com.asrevo.cvhome.store.core.entity.catalog.catalog.CatalogCategoryEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PageableCatalogEntryRepository
        extends PagingAndSortingRepository<CatalogCategoryEntry, Long> {
    @Query(
            value =
                    """
            select distinct c from CatalogCategoryEntry c
            join fetch c.category cc
            join fetch c.catalog cl
            join fetch cl.merchantStore clm
            left join fetch cc.descriptions ccd
            where cl.id=?1 and
            clm.id=?2 and
            ccd.language.id=?3""",
            countQuery =
                    """
                    select  count(c) from CatalogCategoryEntry c
                    join c.category cc
                    join c.catalog cl
                    join cl.merchantStore clm
                    join cc.descriptions ccd
                    where cl.id=?1 and clm.id=?2 and ccd.language.id=?3""")
    Page<CatalogCategoryEntry> listByCatalog(
            Long catalogId, Integer storeId, Integer languageId, String name, Pageable pageable);
}
