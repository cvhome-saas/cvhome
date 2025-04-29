package com.asrevo.cvhome.catalog.repositories.category;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PageableCategoryRepository extends PagingAndSortingRepository<Category, Long> {

    @Query(
            value =
                    """
                            select distinct c from Category c left join fetch c.descriptions cd
                             where c.storeMerchantId=?1 and
                             cd.languageCode=?2 and (cd.name like %?3% or ?3 is null) order by c.depth,
                             c.sortOrder asc""",
            countQuery =
                    """
                            select  count(c) from Category c join c.descriptions cd where c.storeMerchantId=?1 and cd.languageCode=?2 and (cd.name like %?3% or ?3 is
                             null)""")
    Page<Category> listByStore(
            StoreMerchantId storeMerchantId,
            LanguageCode languageId,
            String name,
            Pageable pageable);
}
