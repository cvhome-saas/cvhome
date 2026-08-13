package com.asrevo.cvhome.content.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.ContentType;

public interface ContentRepository extends JpaRepository<Content, Long> {
    Optional<Content> findByIdAndStoreMerchantId(Long id, StoreMerchantId store);

    List<Content> findAllByStoreMerchantIdOrderByAuditSectionDateModifiedDesc(StoreMerchantId store);

    List<Content> findAllByStoreMerchantIdAndStatusAndPublishAtLessThanEqual(
            StoreMerchantId store, ContentStatus status, Instant now);

    List<Content> findAllByStatusAndPublishAtLessThanEqual(ContentStatus status, Instant now);

    List<Content> findAllByStatusAndUnpublishAtLessThanEqual(ContentStatus status, Instant now);

    @Query("""
            select c from Content c join c.descriptions d
            where c.storeMerchantId = :store and c.status = :status and c.contentType = :type
              and d.languageCode = :language and d.seUrl = :slug
            """)
    Optional<Content> findRoute(@Param("store") StoreMerchantId store,
                                @Param("language") com.asrevo.cvhome.commons.domain.LanguageCode language,
                                @Param("type") ContentType type, @Param("status") ContentStatus status,
                                @Param("slug") String slug);

    @Query("""
            select c from Content c join c.descriptions d
            where c.storeMerchantId = :store and c.status = :status and d.languageCode = :language
              and (lower(d.name) like lower(concat('%', :query, '%'))
                or lower(coalesce(d.title, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(d.description, '')) like lower(concat('%', :query, '%'))
                or function('similarity', lower(d.name), lower(:query)) >= 0.2)
            order by function('similarity', lower(d.name), lower(:query)) desc,
              c.auditSection.dateModified desc
            """)
    List<Content> searchPublished(@Param("store") StoreMerchantId store,
                                  @Param("language") com.asrevo.cvhome.commons.domain.LanguageCode language,
                                  @Param("status") ContentStatus status, @Param("query") String query,
                                  org.springframework.data.domain.Pageable pageable);

    List<Content> findAllByStoreMerchantIdAndStatusOrderByContentTypeAscIdAsc(
            StoreMerchantId store, ContentStatus status);
}
