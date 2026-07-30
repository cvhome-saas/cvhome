package com.asrevo.cvhome.payment.entity.payment;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.converter.CurrencyCodeConverter;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "TRANSACTION", uniqueConstraints = {
        @UniqueConstraint(name = "UC_TRANSACTION_REF_STORE", columnNames = {"REF", "STORE_MERCHANT_ID"})
})
@Getter
@Setter
@Slf4j
public class Transaction extends SalesManagerEntity<Long, Transaction> implements Serializable, Auditable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "TRANSACTION_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "TRANSACTION_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Column(name = "INTERNAL_REF", nullable = false, length = 70)
    private String internalRef;

    @Column(name = "REF", nullable = false, length = 70)
    private String requestRef;

    @Embedded
    @AttributeOverride(name = "storeMerchantId", column = @Column(name = "STORE_MERCHANT_ID", length = 50))
    private StoreMerchantId storeMerchantId;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "CURRENCY_CODE", length = 6, nullable = false)
    @Convert(converter = CurrencyCodeConverter.class)
    private CurrencyCode currency;

    @Column(name = "TRANSACTION_DATE")
    private Instant transactionDate;

    @Column(name = "PAYMENT_TYPE")
    @Enumerated(value = EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "EXTERNAL_ID")
    private String paymentGatewayExternalId;

    @Column(name = "REDIRECT_URL", length = 1000)
    private String redirectUrl;

    @Column(name = "SUCCESS_URL", length = 1000)
    private String successUrl;

    @Column(name = "CANCEL_URL", length = 1000)
    private String cancelUrl;

    @Column(name = "EXPIRE_AT")
    private Instant expireAt;

    @Column(name = "DETAILS", columnDefinition = "text")
    private String details;

    @Column(name = "TRANSACTION_NO")
    private String transactionNo;

}
