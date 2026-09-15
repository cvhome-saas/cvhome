package com.asrevo.cvhome.checkout.entity;

import java.io.Serial;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * The user-visible status trail — what the console and the shopper's order page show. The complete ledger,
 * including signals that changed nothing, is {@link OrderEvent}.
 */
@Entity
@Table(name = "SALES_ORDER_HISTORY")
@Getter
@Setter
public class OrderStatusHistory extends SalesManagerEntity<Long, OrderStatusHistory> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "HISTORY_ID")
    @SequenceGenerator(name = "sales_order_history_seq", sequenceName = "sales_order_history_seq",
            allocationSize = SchemaConstant.ID_ALLOCATION_SIZE)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sales_order_history_seq")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "COMMENTS", columnDefinition = "text")
    private String comments;

    @Column(name = "ACTOR", length = 100)
    private String actor;

    @Column(name = "DATE_ADDED", nullable = false)
    private Instant dateAdded;

    public OrderStatusHistory() {
    }

    public OrderStatusHistory(Order order, OrderStatus status, String comments, String actor, Instant dateAdded) {
        this.order = order;
        this.status = status;
        this.comments = comments;
        this.actor = actor;
        this.dateAdded = dateAdded;
    }
}
