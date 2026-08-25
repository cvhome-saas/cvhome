package com.asrevo.cvhome.content.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.Setter;

/**
 * An immutable snapshot of a content item as it was saved at {@code version}. The snapshot is the readable DTO as
 * JSON, so a restore replays it through the same mapper a {@code PUT} uses.
 */
@Entity
@Table(name = "CONTENT_REVISION", uniqueConstraints = @UniqueConstraint(name = "content_revision_unique",
        columnNames = {"CONTENT_ID", "VERSION"}))
@Getter
@Setter
public class ContentRevision implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "content_seq", sequenceName = "content_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_seq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)
    private String storeMerchantId;

    @Column(name = "CONTENT_ID", nullable = false)
    private Long contentId;

    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "SNAPSHOT", nullable = false)
    private String snapshot;

    @Column(name = "AUTHOR", length = 120)
    private String author;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt = Instant.now();

}
