package com.asrevo.cvhome.checkout.entity.order.orderaccount;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ORDER_ACCOUNT_PRODUCT")
@Getter
@Setter
public class OrderAccountProduct implements Serializable {

    @Serial
    private static final long serialVersionUID = -7437197293537758668L;

    @Id
    @Column(name = "ORDER_ACCOUNT_PRODUCT_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "ORDER_ACCOUNT_PRODUCT_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long orderAccountProductId;

    @ManyToOne
    @JoinColumn(name = "ORDER_ACCOUNT_ID", nullable = false)
    private OrderAccount orderAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_PRODUCT_ID", nullable = false)
    private OrderProduct orderProduct;

    @Column(name = "ORDER_ACCOUNT_PRODUCT_ST_DT", length = 0, nullable = false)
    private LocalDate orderAccountProductStartDate;

    @Column(name = "ORDER_ACCOUNT_PRODUCT_END_DT", length = 0)
    private LocalDate orderAccountProductEndDate;

    @Column(name = "ORDER_ACCOUNT_PRODUCT_EOT", length = 0)
    private Instant orderAccountProductEot;

    @Column(name = "ORDER_ACCOUNT_PRODUCT_ACCNT_DT", length = 0)
    private LocalDate orderAccountProductAccountedDate;

    @Column(name = "ORDER_ACCOUNT_PRODUCT_L_ST_DT", length = 0)
    private Instant orderAccountProductLastStatusDate;

    @Column(name = "ORDER_ACCOUNT_PRODUCT_L_TRX_ST", nullable = false)
    private Integer orderAccountProductLastTransactionStatus;

    @Column(name = "ORDER_ACCOUNT_PRODUCT_PM_FR_TY", nullable = false)
    private Integer orderAccountProductPaymentFrequencyType;

    @Column(name = "ORDER_ACCOUNT_PRODUCT_STATUS", nullable = false)
    private Integer orderAccountProductStatus;

}
