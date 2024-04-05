package com.asrevo.cvhome.store.core.entity.order.orderaccount;

import com.asrevo.cvhome.store.core.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.store.core.utils.CloneUtils;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "ORDER_ACCOUNT_PRODUCT")
@Getter
@Setter
public class OrderAccountProduct implements Serializable {
    @Serial
    private static final long serialVersionUID = -7437197293537758668L;

    @Id
    @Column(name = "ORDER_ACCOUNT_PRODUCT_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
            pkColumnValue = "ORDERACCOUNTPRODUCT_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long orderAccountProductId;

    @ManyToOne
    @JoinColumn(name = "ORDER_ACCOUNT_ID", nullable = false)
    private OrderAccount orderAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_PRODUCT_ID", nullable = false)
    private OrderProduct orderProduct;

    @Temporal(TemporalType.DATE)
    @Column(name = "ORDER_ACCOUNT_PRODUCT_ST_DT", length = 0, nullable = false)
    private Date orderAccountProductStartDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "ORDER_ACCOUNT_PRODUCT_END_DT", length = 0)
    private Date orderAccountProductEndDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ORDER_ACCOUNT_PRODUCT_EOT", length = 0)
    private Date orderAccountProductEot;

    @Temporal(TemporalType.DATE)
    @Column(name = "ORDER_ACCOUNT_PRODUCT_ACCNT_DT", length = 0)
    private Date orderAccountProductAccountedDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ORDER_ACCOUNT_PRODUCT_L_ST_DT", length = 0)
    private Date orderAccountProductLastStatusDate;

    @Column(name = "ORDER_ACCOUNT_PRODUCT_L_TRX_ST", nullable = false)
    private Integer orderAccountProductLastTransactionStatus;

    @Column(name = "ORDER_ACCOUNT_PRODUCT_PM_FR_TY", nullable = false)
    private Integer orderAccountProductPaymentFrequencyType;

    @Column(name = "ORDER_ACCOUNT_PRODUCT_STATUS", nullable = false)
    private Integer orderAccountProductStatus;

    public OrderAccountProduct() {
    }


    public Date getOrderAccountProductStartDate() {
        return CloneUtils.clone(orderAccountProductStartDate);
    }

    public void setOrderAccountProductStartDate(Date orderAccountProductStartDate) {
        this.orderAccountProductStartDate = CloneUtils.clone(orderAccountProductStartDate);
    }

    public Date getOrderAccountProductEndDate() {
        return CloneUtils.clone(orderAccountProductEndDate);
    }

    public void setOrderAccountProductEndDate(Date orderAccountProductEndDate) {
        this.orderAccountProductEndDate = CloneUtils.clone(orderAccountProductEndDate);
    }

    public Date getOrderAccountProductEot() {
        return CloneUtils.clone(orderAccountProductEot);
    }

    public void setOrderAccountProductEot(Date orderAccountProductEot) {
        this.orderAccountProductEot = CloneUtils.clone(orderAccountProductEot);
    }

    public Date getOrderAccountProductAccountedDate() {
        return CloneUtils.clone(orderAccountProductAccountedDate);
    }

    public void setOrderAccountProductAccountedDate(
            Date orderAccountProductAccountedDate) {
        this.orderAccountProductAccountedDate = CloneUtils.clone(orderAccountProductAccountedDate);
    }

    public Date getOrderAccountProductLastStatusDate() {
        return CloneUtils.clone(orderAccountProductLastStatusDate);
    }

    public void setOrderAccountProductLastStatusDate(
            Date orderAccountProductLastStatusDate) {
        this.orderAccountProductLastStatusDate = CloneUtils.clone(orderAccountProductLastStatusDate);
    }

}
