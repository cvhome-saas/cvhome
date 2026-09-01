package com.asrevo.cvhome.content.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.Setter;

/**
 * One page's layout document for one store: the builder's {@code draft} and the {@code published} copy the
 * storefront serves. Not a {@link Content} row on purpose — a layout has no slug, no per-locale description
 * rows and no status machine; its whole lifecycle is the pair of documents plus an optimistic
 * {@code draftVersion}.
 */
@Entity
@Table(name = "PAGE_LAYOUT")
@Getter
@Setter
public class PageLayout implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_seq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "STORE_MERCHANT_ID", length = 50, nullable = false)
    private String storeMerchantId;

    @Column(name = "PAGE", length = 32, nullable = false)
    private String page;

    /** The {@code LayoutDocument} the builder edits; every save replaces it whole. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "DRAFT", nullable = false)
    private String draft;

    /** The {@code LayoutDocument} shoppers see; null until first publish. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "PUBLISHED")
    private String published;

    @Column(name = "DRAFT_VERSION", nullable = false)
    private int draftVersion = 1;

    /**
     * Database-enforced companion to {@code draftVersion}: the service's read-then-check alone leaves a window
     * where two concurrent saves both pass and the second silently clobbers the first. Hibernate's version guard
     * turns that second commit into an optimistic-lock failure — a 409, never a lost update.
     */
    @Version
    @Column(name = "LOCK_VERSION", nullable = false)
    private long lockVersion;

    @Column(name = "PUBLISHED_VERSION")
    private Integer publishedVersion;

    @Column(name = "PUBLISHED_AT")
    private Instant publishedAt;

    @Column(name = "DATE_CREATED")
    private Instant dateCreated;

    @Column(name = "LAST_MODIFIED")
    private Instant lastModified;

    @Column(name = "MODIFIED_BY", length = 120)
    private String modifiedBy;

}
