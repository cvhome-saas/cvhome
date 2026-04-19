package com.asrevo.cvhome.checkout.entity.order.orderstatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ORDER_STATUS_HISTORY")
@Getter
@Setter
public class OrderStatusHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 3438730310126102187L;

    @Id
    @Column(name = "ORDER_STATUS_HISTORY_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "ORDER_STATUS_HISTORY_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @JsonIgnore
    @ManyToOne(targetEntity = Order.class)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Order order;

    @Enumerated(value = EnumType.STRING)
    private OrderStatus status;

    @Column(name = "DATE_ADDED", nullable = false)
    private Instant dateAdded;

    @Column(name = "CUSTOMER_NOTIFIED")
    private java.lang.Integer customerNotified;

    @Column(name = "COMMENTS", columnDefinition = "text")
    private String comments;

    public OrderStatusHistory() {
    }

}

