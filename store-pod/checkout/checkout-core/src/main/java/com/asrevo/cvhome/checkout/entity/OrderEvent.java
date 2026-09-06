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
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.checkout.model.order.OrderEventOutcome;
import com.asrevo.cvhome.checkout.model.order.OrderEventSource;
import com.asrevo.cvhome.checkout.model.order.OrderEventType;
import com.asrevo.cvhome.checkout.model.order.PendingAction;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.InventoryStatus;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * One row of the order ledger: every transition and every inbound signal, including the ones that changed nothing.
 * {@code (order, source, sourceRef)} is unique where {@code sourceRef} is set, which is what makes a redelivered
 * signal a recorded {@code DUPLICATE} rather than a second application.
 */
@Entity
@Table(name = "SALES_ORDER_EVENT")
@Getter
@Setter
public class OrderEvent extends SalesManagerEntity<Long, OrderEvent> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "EVENT_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "SALES_ORDER_EVENT_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "EVENT_TYPE", nullable = false, length = 40)
    private OrderEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "SOURCE", nullable = false, length = 20)
    private OrderEventSource source;

    @Column(name = "SOURCE_REF", length = 120)
    private String sourceRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "OUTCOME", nullable = false, length = 10)
    private OrderEventOutcome outcome;

    @Enumerated(EnumType.STRING)
    @Column(name = "ORDER_STATUS_AFTER", length = 20)
    private OrderStatus orderStatusAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_STATUS_AFTER", length = 30)
    private PaymentStatus paymentStatusAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "INVENTORY_STATUS_AFTER", length = 30)
    private InventoryStatus inventoryStatusAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "PENDING_ACTION_AFTER", length = 20)
    private PendingAction pendingActionAfter;

    @Column(name = "PAYLOAD", columnDefinition = "text")
    private String payload;

    @Column(name = "REASON")
    private String reason;

    @Column(name = "OCCURRED_AT", nullable = false)
    private Instant occurredAt;

    public OrderEvent() {
    }

    OrderEvent(Order order, OrderEventType eventType, OrderEventSource source, String sourceRef,
               OrderEventOutcome outcome, String reason, Instant occurredAt) {
        this.order = order;
        this.eventType = eventType;
        this.source = source;
        this.sourceRef = sourceRef;
        this.outcome = outcome;
        this.reason = reason;
        this.occurredAt = occurredAt;
        this.orderStatusAfter = order.getOrderStatus();
        this.paymentStatusAfter = order.getPaymentStatus();
        this.inventoryStatusAfter = order.getInventoryStatus();
        this.pendingActionAfter = order.getPendingAction();
    }

    public boolean matches(OrderEventSource aSource, String aSourceRef) {
        return source == aSource && aSourceRef != null && aSourceRef.equals(sourceRef);
    }
}
