package com.asrevo.cvhome.content.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.asrevo.cvhome.content.model.MediaKind;

import lombok.Getter;
import lombok.Setter;

/**
 * One uploaded file: where it lives in object storage, what it is, and its editable metadata. Deduplicated per
 * store by checksum.
 */
@Entity
@Table(name = "MEDIA_ASSET", uniqueConstraints = @UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "CHECKSUM"}))
@Getter
@Setter
public class MediaAsset implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "content_seq", sequenceName = "content_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_seq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)
    private String storeMerchantId;

    @Column(name = "FOLDER_ID")
    private Long folderId;

    @Column(name = "FILENAME", nullable = false, length = 255)
    private String filename;

    @Column(name = "ORIGINAL_FILENAME", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "MIME_TYPE", nullable = false, length = 120)
    private String mimeType;

    @Column(name = "KIND", length = 12, nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaKind kind;

    @Column(name = "BYTES", nullable = false)
    private long bytes;

    @Column(name = "WIDTH")
    private Integer width;

    @Column(name = "HEIGHT")
    private Integer height;

    @Column(name = "CHECKSUM", nullable = false, length = 64)
    private String checksum;

    @Column(name = "STORAGE_KEY", nullable = false, length = 255)
    private String storageKey;

    @Column(name = "PUBLIC_URL", nullable = false, length = 500)
    private String publicUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ALT_TEXTS")
    private String altTexts;

    @Column(name = "TITLE", length = 200)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "TAGS")
    private String tags;

    @Column(name = "UPLOADED_BY", length = 120)
    private String uploadedBy;

    @Column(name = "UPLOADED_AT", nullable = false)
    private Instant uploadedAt = Instant.now();

}
