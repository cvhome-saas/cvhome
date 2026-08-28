package com.asrevo.cvhome.content.entity;

import java.io.Serial;
import java.io.Serializable;

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

import com.asrevo.cvhome.content.model.MediaOwnerKind;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.Getter;
import lombok.Setter;

/**
 * One reference from an owner's field to a media asset. Rebuilt for an owner on every save.
 *
 * <p>
 * The owner is {@code (ownerKind, ownerRef)}, not just a content id, because appearance and catalogue data hold
 * media too. {@code contentId} / {@code contentType} stay populated for {@link MediaOwnerKind#CONTENT} rows so
 * nothing about the existing usage index changes meaning; they are null for every other kind. {@code ownerTitle}
 * is stored rather than resolved, so that answering "which product uses this image" never requires calling back
 * into catalog — that would invert the dependency this service is meant to sit at the bottom of.
 * </p>
 */
@Entity
@Table(name = "MEDIA_USAGE", uniqueConstraints = @UniqueConstraint(name = "media_usage_unique",
        columnNames = {"ASSET_ID", "OWNER_KIND", "OWNER_REF", "FIELD"}))
@Getter
@Setter
public class MediaUsageRow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "content_seq", sequenceName = "content_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_seq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "ASSET_ID", nullable = false)
    private Long assetId;

    @Column(name = "OWNER_KIND", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaOwnerKind ownerKind;

    /**
     * The owner's identity within its kind — a content id as text, the store id for site settings, a product id.
     */
    @Column(name = "OWNER_REF", nullable = false, length = 120)
    private String ownerRef;

    /**
     * What the console shows beside the usage. Supplied by the owner; never looked up remotely.
     */
    @Column(name = "OWNER_TITLE", length = 200)
    private String ownerTitle;

    @Column(name = "CONTENT_ID")
    private Long contentId;

    @Column(name = "CONTENT_TYPE", length = 10)
    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    @Column(name = "FIELD", nullable = false, length = 40)
    private String field;

}
