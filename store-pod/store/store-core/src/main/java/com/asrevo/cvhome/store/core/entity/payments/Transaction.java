package com.asrevo.cvhome.store.core.entity.payments;

import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONAware;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "SM_TRANSACTION")
@Getter
@Setter
@Slf4j
public class Transaction extends SalesManagerEntity<Long, Transaction> implements Serializable, Auditable, JSONAware {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "TRANSACTION_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "TRANSACT_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID", nullable = true)
    private Order order;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "TRANSACTION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;

    @Column(name = "TRANSACTION_TYPE")
    @Enumerated(value = EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "PAYMENT_TYPE")
    @Enumerated(value = EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "DETAILS", columnDefinition = "text")
    private String details;

    @Transient
    private Map<String, String> transactionDetails = new HashMap<String, String>();

    @Override
    public String toJSONString() {

        if (this.getTransactionDetails() != null && !this.getTransactionDetails().isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(this.getTransactionDetails());
            } catch (Exception e) {
                log.error("Cannot parse transactions map", e);
            }

        }

        return null;
    }

    public String getTransactionTypeName() {
        return this.getTransactionType() != null ? this.getTransactionType().name() : "";
    }
}
