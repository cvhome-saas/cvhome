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

import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.Getter;
import lombok.Setter;

/**
 * One reference from a content item's field to a media asset. Rebuilt for an item on every save.
 */
@Entity
@Table(name = "MEDIA_USAGE", uniqueConstraints = @UniqueConstraint(columnNames = {"ASSET_ID", "CONTENT_ID", "FIELD"}))
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

    @Column(name = "CONTENT_ID", nullable = false)
    private Long contentId;

    @Column(name = "CONTENT_TYPE", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    @Column(name = "FIELD", nullable = false, length = 40)
    private String field;

}
