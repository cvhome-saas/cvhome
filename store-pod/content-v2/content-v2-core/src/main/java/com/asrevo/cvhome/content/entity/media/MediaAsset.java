package com.asrevo.cvhome.content.entity.media;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Version;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.model.MediaKind;
import com.asrevo.cvhome.content.model.MediaProcessingStatus;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MEDIA_ASSET")
@Getter
@Setter
public class MediaAsset extends SalesManagerEntity<Long, MediaAsset> {
    @Id
    @Column(name = "ASSET_ID")
    @TableGenerator(name = "media_asset_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "MEDIA_ASSET_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "media_asset_gen")
    private Long id;

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Column(name = "ORIGINAL_FILENAME", nullable = false, length = 500)
    private String originalFilename;
    @Column(name = "NORMALIZED_FILENAME", nullable = false, length = 500)
    private String normalizedFilename;
    @Column(name = "DETECTED_MIME", nullable = false, length = 100)
    private String detectedMime;
    @Enumerated(EnumType.STRING)
    @Column(name = "MEDIA_KIND", nullable = false, length = 20)
    private MediaKind mediaKind;
    @Column(name = "BYTE_SIZE", nullable = false)
    private long byteSize;
    @Column(name = "CHECKSUM", nullable = false, length = 64)
    private String checksum;
    @Column(name = "WIDTH")
    private Integer width;
    @Column(name = "HEIGHT")
    private Integer height;
    @Column(name = "PAGE_COUNT")
    private Integer pageCount;
    @Column(name = "STORAGE_KEY", nullable = false, length = 1000)
    private String storageKey;
    @Enumerated(EnumType.STRING)
    @Column(name = "PROCESSING_STATUS", nullable = false, length = 20)
    private MediaProcessingStatus processingStatus;
    @Column(name = "FAILURE_REASON", length = 1000)
    private String failureReason;
    @Column(name = "DELETED_AT")
    private Instant deletedAt;
    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaVariant> variants = new ArrayList<>();

    public void addVariant(MediaVariant variant) {
        variant.setAsset(this);
        variants.add(variant);
    }
}
