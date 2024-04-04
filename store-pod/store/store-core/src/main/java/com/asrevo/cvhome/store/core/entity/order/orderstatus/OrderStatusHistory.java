package com.asrevo.cvhome.store.core.entity.order.orderstatus;

import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.utils.CloneUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "ORDER_STATUS_HISTORY")
@Getter
@Setter
public class OrderStatusHistory implements Serializable {
    private static final long serialVersionUID = 3438730310126102187L;

    @Id
    @Column(name = "ORDER_STATUS_HISTORY_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
            pkColumnValue = "STATUS_HIST_ID_NEXT_VALUE")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @JsonIgnore
    @ManyToOne(targetEntity = Order.class)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Order order;

    @Enumerated(value = EnumType.STRING)
    private OrderStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_ADDED", nullable = false)
    private Date dateAdded;

    @Column(name = "CUSTOMER_NOTIFIED")
    private java.lang.Integer customerNotified;

    @Column(name = "COMMENTS", columnDefinition = "text")
    private String comments;

    public OrderStatusHistory() {
    }

    public Date getDateAdded() {
        return CloneUtils.clone(dateAdded);
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = CloneUtils.clone(dateAdded);
    }


}