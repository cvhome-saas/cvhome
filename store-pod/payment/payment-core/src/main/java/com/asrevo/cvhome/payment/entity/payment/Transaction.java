package com.asrevo.cvhome.payment.entity.payment;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
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

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.entity.payments.TransactionType;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "TRANSACTION")
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

    @Column(name = "ORDER_ID")
    private Long orderId;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "TRANSACTION_DATE")
    private Instant transactionDate;

    @Column(name = "TRANSACTION_TYPE")
    @Enumerated(value = EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "PAYMENT_TYPE")
    @Enumerated(value = EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "DETAILS", columnDefinition = "text")
    private String details;

}
