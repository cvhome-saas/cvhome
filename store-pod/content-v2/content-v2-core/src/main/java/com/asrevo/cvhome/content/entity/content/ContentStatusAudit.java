package com.asrevo.cvhome.content.entity.content;

import java.time.Instant;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONTENT_STATUS_AUDIT")
@Getter
@Setter
public class ContentStatusAudit {
    @Id
    @Column(name = "STATUS_AUDIT_ID")
    @TableGenerator(name = "content_status_audit_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "CONTENT_STATUS_AUDIT_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "content_status_audit_gen")
    private Long id;
    @Column(name = "CONTENT_ID", nullable = false)
    private Long contentId;
    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;
    @Enumerated(EnumType.STRING)
    @Column(name = "FROM_STATUS", length = 20)
    private ContentStatus fromStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "TO_STATUS", nullable = false, length = 20)
    private ContentStatus toStatus;
    @Column(name = "ACTOR", nullable = false)
    private String actor;
    @Column(name = "REASON", length = 1000)
    private String reason;
    @Column(name = "OCCURRED_AT", nullable = false)
    private Instant occurredAt;
}
