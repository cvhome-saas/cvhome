package com.asrevo.cvhome.content.entity.content;

import java.time.Instant;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONTENT_AUDIT")
@Getter
@Setter
public class ContentAudit {
    @Id
    @Column(name = "AUDIT_ID")
    @TableGenerator(name = "content_audit_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "CONTENT_AUDIT_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "content_audit_gen")
    private Long id;
    @Column(name = "CONTENT_ID", nullable = false)
    private Long contentId;
    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;
    @Column(name = "ACTION", nullable = false, length = 50)
    private String action;
    @Column(name = "ACTOR", nullable = false)
    private String actor;
    @Column(name = "BEFORE_SUMMARY", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String beforeSummary;
    @Column(name = "AFTER_SUMMARY", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String afterSummary;
    @Column(name = "OCCURRED_AT", nullable = false)
    private Instant occurredAt;
}
