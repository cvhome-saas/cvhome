package com.asrevo.cvhome.content.repositories.content;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PageContentRepository extends PagingAndSortingRepository<Content, Long> {

    @Query(
            value =
                    """
                            select c from Content c
                            left join fetch c.descriptions cd
                            where c.contentType = ?1
                            and c.storeMerchantId = ?2
                            order by c.sortOrder asc
                            """,
            countQuery =
                    """
                            select count(distinct c) from Content c
                            where c.contentType = ?1
                            and c.storeMerchantId = ?2""")
    Page<Content> findByContentType(
            ContentType contentType, StoreMerchantId storeMerchantId, Pageable pageable);

    @Query(
            value =
                    """
                            select c from Content c
                            left join fetch c.descriptions cd
                            where c.contentType = ?1
                            and c.storeMerchantId = ?2
                            and cd.languageCode = ?3
                            order by c.sortOrder asc""",
            countQuery =
                    """
                            select count(distinct c) from Content c
                            where c.contentType = ?1
                            and c.storeMerchantId = ?2""")
    Page<Content> findByContentType(
            ContentType contentTypes,
            StoreMerchantId storeMerchantId,
            LanguageCode languageCode,
            Pageable pageable);
}
