package com.asrevo.cvhome.content.entity.content;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONTENT_REDIRECT")
@Getter
@Setter
public class ContentRedirect {
    @Id
    @Column(name = "REDIRECT_ID")
    @TableGenerator(name = "content_redirect_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "CONTENT_REDIRECT_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "content_redirect_gen")
    private Long id;
    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;
    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "LANGUAGE_CODE", nullable = false, length = 6))
    private LanguageCode languageCode;
    @Column(name = "OLD_PATH", nullable = false, length = 500)
    private String oldPath;
    @Column(name = "DESTINATION_CONTENT_ID", nullable = false)
    private Long destinationContentId;
    @Column(name = "HTTP_STATUS", nullable = false)
    private short httpStatus = 301;
}
