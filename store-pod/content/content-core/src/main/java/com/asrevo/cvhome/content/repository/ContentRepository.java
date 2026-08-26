package com.asrevo.cvhome.content.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

/**
 * Carried from the legacy service and extended. Every query that returns a row for a caller is scoped to the store;
 * the only exception is the scheduler's {@link #findDue}, which sweeps all stores.
 */
public interface ContentRepository extends JpaRepository<Content, Long>, JpaSpecificationExecutor<Content> {

    @Query("""
            select c from Content c
            left join fetch c.descriptions cd
            where c.id = :id and c.storeMerchantId = :store""")
    Optional<Content> findByIdAndStore(@Param("id") Long id, @Param("store") StoreMerchantId store);

    /**
     * Bumps the row's version and audit stamp when only its child rows changed.
     *
     * Title and body live in {@code content_description}, so editing them leaves {@code content} clean:
     * Hibernate would neither increment {@code @Version} nor let the audit listener stamp
     * {@code dateModified}, which left the list's "updated" column empty and made the next revision snapshot
     * collide with the previous one on {@code (content_id, version)}. {@code update versioned} is Hibernate's
     * way to say "this counts as a change to the row itself".
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update versioned Content c
            set c.auditSection.dateModified = :now, c.auditSection.modifiedBy = :actor
            where c.id = :id""")
    void touch(@Param("id") Long id, @Param("now") Instant now, @Param("actor") String actor);

    @Query("""
            select c from Content c
            left join fetch c.descriptions cd
            where c.code = :code and c.storeMerchantId = :store""")
    Optional<Content> findByCodeFetchAllLanguages(@Param("code") String code, @Param("store") StoreMerchantId store);

    @Query("""
            select c from Content c
            left join fetch c.descriptions cd
            where c.code = :code and c.contentType = :type and c.storeMerchantId = :store""")
    Optional<Content> findByCodeAndType(@Param("code") String code, @Param("type") ContentType type,
                                        @Param("store") StoreMerchantId store);

    boolean existsByStoreMerchantIdAndCode(StoreMerchantId store, String code);

    boolean existsByStoreMerchantIdAndCodeAndIdNot(StoreMerchantId store, String code, Long id);

    /**
     * Legacy lookup: a visible page whose per-language friendly URL matches.
     */
    @Query("""
            select distinct c from Content c
            left join fetch c.descriptions cd
            where c.storeMerchantId = :store and c.contentType = :type and c.visible = true
            order by c.sortOrder, c.id""")
    List<Content> findVisibleByType(@Param("store") StoreMerchantId store, @Param("type") ContentType type);

    @Query("""
            select distinct c from Content c
            left join fetch c.descriptions cd
            where c.storeMerchantId = :store and c.contentType = :type
            order by c.sortOrder, c.id""")
    List<Content> findAllByType(@Param("store") StoreMerchantId store, @Param("type") ContentType type);

    long countByStoreMerchantIdAndContentType(StoreMerchantId store, ContentType type);

    long countByStoreMerchantIdAndContentTypeInAndStatus(StoreMerchantId store, List<ContentType> types,
                                                         ContentStatus status);

    @Query("""
            select count(c) from Content c
            where c.storeMerchantId = :store and c.status = :status and c.contentType in :types
              and c.auditSection.dateModified < :before""")
    long countStale(@Param("store") StoreMerchantId store, @Param("status") ContentStatus status,
                    @Param("types") List<ContentType> types, @Param("before") Instant before);

    /**
     * Scheduler sweep: rows in {@code status} whose {@code publishAt} (or {@code unpublishAt}) is due.
     */
    @Query("""
            select c from Content c
            where c.status = :status and c.publishAt is not null and c.publishAt <= :now""")
    List<Content> findDue(@Param("status") ContentStatus status, @Param("now") Instant now);

    @Query("""
            select c from Content c
            where c.status = :status and c.unpublishAt is not null and c.unpublishAt <= :now""")
    List<Content> findExpired(@Param("status") ContentStatus status, @Param("now") Instant now);

}
