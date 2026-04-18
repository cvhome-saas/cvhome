package com.asrevo.cvhome.checkout.entity.order.orderaccount;

import java.io.Serial;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.utils.CloneUtils;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ORDER_ACCOUNT")
@Getter
@Setter
public class OrderAccount extends SalesManagerEntity<Long, OrderAccount> {

    @Serial
    private static final long serialVersionUID = -2429388347536330540L;

    @Id
    @Column(name = "ORDER_ACCOUNT_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "ORDER_ACCOUNT_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Order order;

    @Temporal(TemporalType.DATE)
    @Column(name = "ORDER_ACCOUNT_START_DATE", nullable = false, length = 0)
    private Date orderAccountStartDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "ORDER_ACCOUNT_END_DATE", length = 0)
    private Date orderAccountEndDate;

    @Column(name = "ORDER_ACCOUNT_BILL_DAY", nullable = false)
    private Integer orderAccountBillDay;

    @OneToMany(mappedBy = "orderAccount", cascade = CascadeType.ALL)
    private Set<OrderAccountProduct> orderAccountProducts = new HashSet<>();

    public OrderAccount() {
    }

    public Date getOrderAccountStartDate() {
        return CloneUtils.clone(orderAccountStartDate);
    }

    public void setOrderAccountStartDate(Date orderAccountStartDate) {
        this.orderAccountStartDate = CloneUtils.clone(orderAccountStartDate);
    }

    public Date getOrderAccountEndDate() {
        return CloneUtils.clone(orderAccountEndDate);
    }

    public void setOrderAccountEndDate(Date orderAccountEndDate) {
        this.orderAccountEndDate = CloneUtils.clone(orderAccountEndDate);
    }

}
