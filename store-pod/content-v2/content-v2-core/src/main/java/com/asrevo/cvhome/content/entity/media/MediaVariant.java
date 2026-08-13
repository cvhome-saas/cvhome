package com.asrevo.cvhome.content.entity.media;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MEDIA_VARIANT")
@Getter
@Setter
public class MediaVariant {
    @Id
    @Column(name = "VARIANT_ID")
    @TableGenerator(name = "media_variant_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "MEDIA_VARIANT_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "media_variant_gen")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "ASSET_ID", nullable = false)
    private MediaAsset asset;
    @Column(name = "VARIANT_NAME", nullable = false, length = 30)
    private String variantName;
    @Column(name = "FORMAT", nullable = false, length = 10)
    private String format;
    @Column(name = "WIDTH", nullable = false)
    private int width;
    @Column(name = "HEIGHT", nullable = false)
    private int height;
    @Column(name = "BYTE_SIZE", nullable = false)
    private long byteSize;
    @Column(name = "STORAGE_KEY", nullable = false, length = 1000)
    private String storageKey;
}
