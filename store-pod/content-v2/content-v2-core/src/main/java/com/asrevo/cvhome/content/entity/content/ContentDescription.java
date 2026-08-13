package com.asrevo.cvhome.content.entity.content;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.model.ContentType;
import com.asrevo.cvhome.content.model.TranslationState;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.BaseDescription;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONTENT_DESCRIPTION")
@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "CONTENT_DESCRIPTION_SEQ_NEXT_VAL",
        allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
        initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@AttributeOverrides({
        @AttributeOverride(name = "name", column = @Column(name = "NAME", nullable = false, length = 255)),
        @AttributeOverride(name = "title", column = @Column(name = "TITLE", length = 255))
})
@Getter
@Setter
public class ContentDescription extends BaseDescription implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "CONTENT_ID", nullable = false)
    private Content content;

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "CONTENT_TYPE", nullable = false, length = 20)
    private ContentType contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "TRANSLATION_STATE", nullable = false, length = 20)
    private TranslationState translationState = TranslationState.DRAFT;

    @Column(name = "SEF_URL", length = 255)
    private String seUrl;

    @Column(name = "META_TITLE", length = 255)
    private String metatagTitle;

    @Column(name = "META_DESCRIPTION", length = 500)
    private String metatagDescription;

    @Column(name = "META_KEYWORDS", length = 500)
    private String metatagKeywords;

    @Column(name = "CANONICAL_URL", length = 1000)
    private String canonicalUrl;

    @Column(name = "OG_MEDIA_ID")
    private Long ogMediaId;

    @Column(name = "NO_INDEX", nullable = false)
    private boolean noIndex;
}
