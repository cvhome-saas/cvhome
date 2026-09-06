package com.asrevo.cvhome.checkout.entity;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.domain.OrderRef;
import com.asrevo.cvhome.checkout.entity.converter.CartCodeConverter;
import com.asrevo.cvhome.checkout.entity.converter.OrderRefConverter;
import com.asrevo.cvhome.checkout.errors.IllegalOrderTransitionException;
import com.asrevo.cvhome.checkout.model.order.OrderEventOutcome;
import com.asrevo.cvhome.checkout.model.order.OrderEventSource;
import com.asrevo.cvhome.checkout.model.order.OrderEventType;
import com.asrevo.cvhome.checkout.model.order.PendingAction;
import com.asrevo.cvhome.checkout.model.signal.SignalOutcome;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.converter.CurrencyCodeConverter;
import com.asrevo.cvhome.store.core.converter.LanguageCodeConverter;
import com.asrevo.cvhome.store.core.entity.common.InventoryStatus;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.Getter;
import lombok.Setter;

/**
 * The order aggregate. Every status change is a method here: it checks where the order is, moves the three statuses
 * and the pending remote action together, and appends one {@link OrderEvent} — so the ledger and the state can never
 * disagree, and a crash between two remote calls leaves a {@link PendingAction} the recovery job can finish.
 *
 * <p>
 * Inbound signals ({@link #applyPaymentSignal}, {@link #applyReservationExpired}) never throw: a redelivery is a
 * recorded {@code DUPLICATE}, a signal the order cannot use a recorded {@code IGNORED}. Only the console's
 * {@link #fulfil} and {@link #cancel} refuse, with a 409, because a person is there to read the answer.
 * </p>
 */
@Entity
@EntityListeners(AuditListener.class)
@Table(name = "SALES_ORDER", uniqueConstraints = @UniqueConstraint(name = "UK_SALES_ORDER_REF", columnNames = "ORDER_REF"))
@Getter
@Setter
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class Order extends SalesManagerEntity<Long, Order> implements Auditable {

    private static final Set<OrderStatus> CLOSED = EnumSet.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED,
            OrderStatus.RETURNED);

    private static final Set<OrderStatus> AWAITING_PAYMENT = EnumSet.of(OrderStatus.CREATED,
            OrderStatus.PENDING_PAYMENT);

    private static final Set<OrderStatus> CANCELLABLE = EnumSet.of(OrderStatus.CREATED, OrderStatus.PENDING_PAYMENT,
            OrderStatus.CONFIRMED, OrderStatus.PROCESSING);

    private static final Set<PaymentStatus> PAYMENT_LOST = EnumSet.of(PaymentStatus.FAILED, PaymentStatus.CANCELLED,
            PaymentStatus.REJECTED, PaymentStatus.EXPIRED);

    private static final Set<PaymentStatus> PAYMENT_IN_FLIGHT = EnumSet.of(PaymentStatus.PROCESSING,
            PaymentStatus.AUTHORIZED);

    private static final Set<OrderStatus> DELIVERED_OR_LATER = EnumSet.of(OrderStatus.DELIVERED,
            OrderStatus.COMPLETED);

    private static final String ALREADY_PAID = "already paid";

    private static final String ORDER_CLOSED = "order closed";

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ORDER_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "SALES_ORDER_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;

    @Column(name = "ORDER_REF", nullable = false, length = 36)
    @Convert(converter = OrderRefConverter.class)
    private OrderRef orderRef;

    @Column(name = "CART_CODE", nullable = false, length = 36)
    @Convert(converter = CartCodeConverter.class)
    private CartCode cartCode;

    @Column(name = "CUSTOMER_ID", nullable = false)
    private Long customerId;

    @Column(name = "CUA_EXTERNAL_ID", length = 96)
    private String cuaExternalId;

    @Column(name = "CUSTOMER_EMAIL", nullable = false, length = 96)
    private String customerEmail;

    @Column(name = "LANGUAGE_CODE", nullable = false, length = 6)
    @Convert(converter = LanguageCodeConverter.class)
    private LanguageCode language;

    @Column(name = "CURRENCY_CODE", nullable = false, length = 6)
    @Convert(converter = CurrencyCodeConverter.class)
    private CurrencyCode currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_TYPE", nullable = false, length = 20)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ORDER_STATUS", nullable = false, length = 20)
    private OrderStatus orderStatus = OrderStatus.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_STATUS", nullable = false, length = 30)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "INVENTORY_STATUS", nullable = false, length = 30)
    private InventoryStatus inventoryStatus = InventoryStatus.NOT_REQUESTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "PENDING_ACTION", nullable = false, length = 20)
    private PendingAction pendingAction = PendingAction.NONE;

    @Column(name = "PENDING_ACTION_ATTEMPTS", nullable = false)
    private int pendingActionAttempts;

    @Column(name = "PENDING_ACTION_UPDATED_AT", nullable = false)
    private Instant pendingActionUpdatedAt;

    @Column(name = "NEEDS_ATTENTION", nullable = false)
    private boolean needsAttention;

    @Column(name = "ATTENTION_REASON")
    private String attentionReason;

    @Column(name = "RESERVATION_EXPIRE_AT")
    private Instant reservationExpireAt;

    @Column(name = "PAYMENT_TRANSACTION_REF", length = 70)
    private String paymentTransactionRef;

    @Column(name = "REDIRECT_URL", length = 2048)
    private String redirectUrl;

    @Column(name = "SUCCESS_URL", nullable = false, length = 1024)
    private String successUrl;

    @Column(name = "CANCEL_URL", nullable = false, length = 1024)
    private String cancelUrl;

    @Column(name = "EXPIRES_AT")
    private Instant expiresAt;

    @Column(name = "DATE_PURCHASED", nullable = false)
    private Instant datePurchased;

    @Column(name = "SUBTOTAL", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "TOTAL", nullable = false, precision = 19, scale = 4)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "COMMENTS", columnDefinition = "text")
    private String comments;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "BILLING_FIRST_NAME", length = 64)),
            @AttributeOverride(name = "lastName", column = @Column(name = "BILLING_LAST_NAME", length = 64)),
            @AttributeOverride(name = "company", column = @Column(name = "BILLING_COMPANY", length = 100)),
            @AttributeOverride(name = "streetAddress", column = @Column(name = "BILLING_STREET_ADDRESS", length = 256)),
            @AttributeOverride(name = "city", column = @Column(name = "BILLING_CITY", length = 100)),
            @AttributeOverride(name = "stateProvince", column = @Column(name = "BILLING_STATE", length = 100)),
            @AttributeOverride(name = "postcode", column = @Column(name = "BILLING_POSTCODE", length = 20)),
            @AttributeOverride(name = "telephone", column = @Column(name = "BILLING_TELEPHONE", length = 32)),
            @AttributeOverride(name = "country", column = @Column(name = "BILLING_COUNTRY_CODE", length = 6)),
            @AttributeOverride(name = "zoneCode", column = @Column(name = "BILLING_ZONE_CODE", length = 100))})
    private AddressSnapshot billing = new AddressSnapshot();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "DELIVERY_FIRST_NAME", length = 64)),
            @AttributeOverride(name = "lastName", column = @Column(name = "DELIVERY_LAST_NAME", length = 64)),
            @AttributeOverride(name = "company", column = @Column(name = "DELIVERY_COMPANY", length = 100)),
            @AttributeOverride(name = "streetAddress", column = @Column(name = "DELIVERY_STREET_ADDRESS", length = 256)),
            @AttributeOverride(name = "city", column = @Column(name = "DELIVERY_CITY", length = 100)),
            @AttributeOverride(name = "stateProvince", column = @Column(name = "DELIVERY_STATE", length = 100)),
            @AttributeOverride(name = "postcode", column = @Column(name = "DELIVERY_POSTCODE", length = 20)),
            @AttributeOverride(name = "telephone", column = @Column(name = "DELIVERY_TELEPHONE", length = 32)),
            @AttributeOverride(name = "country", column = @Column(name = "DELIVERY_COUNTRY_CODE", length = 6)),
            @AttributeOverride(name = "zoneCode", column = @Column(name = "DELIVERY_ZONE_CODE", length = 100))})
    private AddressSnapshot delivery = new AddressSnapshot();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder asc")
    private List<OrderLine> lines = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder asc")
    private List<OrderTotal> totals = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateAdded asc, id asc")
    private List<OrderStatusHistory> history = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("occurredAt asc, id asc")
    private List<OrderEvent> events = new ArrayList<>();

    public Order() {
    }

    // ---------------------------------------------------------------- placement

    /**
     * Opens an order: {@code CREATED / PENDING / NOT_REQUESTED}, owing a {@code RESERVE}. Add lines, then call
     * {@link #computeTotals()} before saving.
     */
    public static Order place(PlacementDraft draft, String successUrl, String cancelUrl, Instant now) {
        Order order = new Order();
        order.storeMerchantId = draft.store();
        order.orderRef = draft.ref();
        order.cartCode = draft.cartCode();
        order.customerId = draft.customer().getId();
        order.cuaExternalId = draft.customer().getCuaExternalId();
        order.customerEmail = draft.customer().getEmail();
        order.language = draft.language();
        order.currency = draft.currency();
        order.paymentType = draft.paymentType();
        order.billing = draft.billing();
        order.delivery = draft.delivery();
        order.comments = draft.comments();
        order.successUrl = successUrl;
        order.cancelUrl = cancelUrl;
        order.datePurchased = now;
        order.pendingAction = PendingAction.RESERVE;
        order.pendingActionUpdatedAt = now;
        order.record(OrderEventType.PLACED, OrderEventSource.PLACEMENT, null, OrderEventOutcome.APPLIED, null, now);
        order.history.add(new OrderStatusHistory(order, OrderStatus.CREATED, null, null, now));
        return order;
    }

    public OrderLine addLine(String sku, Long productId, String productName, BigDecimal unitPrice, int quantity,
                             String imageUrl) {
        OrderLine line = new OrderLine(this, sku, productId, productName, unitPrice, quantity, imageUrl, lines.size());
        lines.add(line);
        return line;
    }

    /**
     * SUBTOTAL and TOTAL from the lines. There is no shipping and no tax yet, so the two are equal; they are two rows
     * because both frontends read the totals block by code.
     */
    public void computeTotals() {
        subtotal = lines.stream().map(OrderLine::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        total = subtotal;
        totals.clear();
        totals.add(new OrderTotal(this, OrderTotal.SUBTOTAL, "summary", "Subtotal", subtotal, 0));
        totals.add(new OrderTotal(this, OrderTotal.TOTAL, "total", "Total", total, 1));
    }

    // ---------------------------------------------------------------- placement steps

    public void reserved(Long reservationId, Instant reservationExpiry, Instant orderExpiry, Instant now) {
        require(pendingAction == PendingAction.RESERVE, OrderEventType.RESERVED);
        inventoryStatus = InventoryStatus.RESERVED;
        reservationExpireAt = reservationExpiry;
        expiresAt = orderExpiry;
        pending(PendingAction.INITIATE_PAYMENT, now);
        record(OrderEventType.RESERVED, OrderEventSource.INVENTORY, null, OrderEventOutcome.APPLIED,
                reservationId == null ? null : String.format("reservation %d", reservationId), now);
    }

    public void reservationRefused(String reason, Instant now) {
        require(pendingAction == PendingAction.RESERVE, OrderEventType.RESERVATION_REFUSED);
        inventoryStatus = InventoryStatus.RESERVATION_FAILED;
        paymentStatus = PaymentStatus.CANCELLED;
        close(OrderStatus.CANCELLED, reason, null, now);
        pending(PendingAction.NONE, now);
        record(OrderEventType.RESERVATION_REFUSED, OrderEventSource.INVENTORY, null, OrderEventOutcome.APPLIED, reason,
                now);
    }

    /**
     * Payment opened a transaction and is waiting on the shopper (redirect) or on a person (COD, manual transfer).
     * COD confirms right away and commits the stock: the reservation timer could only ever cancel a valid order.
     */
    public void paymentPending(String transactionRef, String redirect, Instant now) {
        require(pendingAction == PendingAction.INITIATE_PAYMENT, OrderEventType.PAYMENT_INITIATED);
        paymentTransactionRef = transactionRef;
        redirectUrl = redirect;
        paymentStatus = PaymentStatus.PENDING;
        if (paymentType == PaymentType.COD) {
            expiresAt = null;
            transition(OrderStatus.CONFIRMED, null, null, now);
            pending(PendingAction.COMMIT, now);
        } else {
            transition(OrderStatus.PENDING_PAYMENT, null, null, now);
            pending(PendingAction.NONE, now);
        }
        record(OrderEventType.PAYMENT_INITIATED, OrderEventSource.PAYMENT, null, OrderEventOutcome.APPLIED,
                paymentStatus.name(), now);
    }

    public void paymentPaidAtInitiate(String transactionRef, Instant now) {
        require(pendingAction == PendingAction.INITIATE_PAYMENT, OrderEventType.PAYMENT_INITIATED);
        paymentTransactionRef = transactionRef;
        markPaid(now);
        record(OrderEventType.PAYMENT_INITIATED, OrderEventSource.PAYMENT, null, OrderEventOutcome.APPLIED,
                PaymentStatus.PAID.name(), now);
    }

    public void paymentFailedAtInitiate(String transactionRef, String reason, Instant now) {
        require(pendingAction == PendingAction.INITIATE_PAYMENT, OrderEventType.PAYMENT_INITIATE_REJECTED);
        paymentTransactionRef = transactionRef;
        paymentStatus = PaymentStatus.FAILED;
        close(OrderStatus.CANCELLED, reason, null, now);
        pending(inventoryStatus == InventoryStatus.RESERVED ? PendingAction.RELEASE : PendingAction.NONE, now);
        record(OrderEventType.PAYMENT_INITIATE_REJECTED, OrderEventSource.PAYMENT, null, OrderEventOutcome.APPLIED,
                reason, now);
    }

    public void committed(Instant now) {
        require(pendingAction == PendingAction.COMMIT, OrderEventType.COMMITTED);
        inventoryStatus = InventoryStatus.COMMITTED;
        pending(PendingAction.NONE, now);
        record(OrderEventType.COMMITTED, OrderEventSource.INVENTORY, null, OrderEventOutcome.APPLIED, null, now);
    }

    public void commitRefused(Instant now) {
        require(pendingAction == PendingAction.COMMIT, OrderEventType.COMMIT_REFUSED);
        inventoryStatus = InventoryStatus.RESERVATION_FAILED;
        pending(PendingAction.NONE, now);
        flag("stock could not be committed after payment", now);
        record(OrderEventType.COMMIT_REFUSED, OrderEventSource.INVENTORY, null, OrderEventOutcome.APPLIED,
                attentionReason, now);
    }

    public void released(Instant now) {
        require(pendingAction == PendingAction.RELEASE, OrderEventType.RELEASED);
        inventoryStatus = InventoryStatus.RELEASED;
        pending(PendingAction.NONE, now);
        record(OrderEventType.RELEASED, OrderEventSource.INVENTORY, null, OrderEventOutcome.APPLIED, null, now);
    }

    // ---------------------------------------------------------------- inbound signals

    /**
     * A payment status from payment-service. Never throws — see the class comment.
     */
    public SignalOutcome applyPaymentSignal(PaymentStatus status, String transactionRef, Instant now) {
        String ref = String.format("%s:%s", transactionRef, status);
        if (isDuplicate(OrderEventSource.PAYMENT, ref)) {
            return recordSignal(OrderEventType.PAYMENT_SIGNAL, OrderEventSource.PAYMENT, ref,
                    OrderEventOutcome.DUPLICATE, null, now);
        }
        paymentTransactionRef = transactionRef;
        String ignored = switch (status) {
            case PAID -> onPaid(now);
            case FAILED, CANCELLED, REJECTED, EXPIRED -> onPaymentLost(status, now);
            case PROCESSING, AUTHORIZED -> onPaymentInFlight(status, now);
            case WAITING_VERIFICATION -> onWaitingVerification(now);
            case REFUNDED -> onRefunded(now);
            case PENDING -> "nothing to learn from PENDING";
        };
        return recordSignal(OrderEventType.PAYMENT_SIGNAL, OrderEventSource.PAYMENT, ref,
                ignored == null ? OrderEventOutcome.APPLIED : OrderEventOutcome.IGNORED, ignored, now);
    }

    private String onPaid(Instant now) {
        if (paymentStatus == PaymentStatus.PAID) {
            return ALREADY_PAID;
        }
        if (isClosed()) {
            paymentStatus = PaymentStatus.PAID;
            flag("paid after cancellation — refund required", now);
            history.add(new OrderStatusHistory(this, orderStatus, attentionReason, null, now));
            record(OrderEventType.PAYMENT_AFTER_CLOSE, OrderEventSource.PAYMENT, null, OrderEventOutcome.APPLIED,
                    attentionReason, now);
            return null;
        }
        markPaid(now);
        return null;
    }

    private String onPaymentLost(PaymentStatus status, Instant now) {
        if (!AWAITING_PAYMENT.contains(orderStatus)) {
            return isClosed() ? ORDER_CLOSED : ALREADY_PAID;
        }
        paymentStatus = status;
        close(OrderStatus.CANCELLED, String.format("payment %s", status.name().toLowerCase()), null, now);
        pending(inventoryStatus == InventoryStatus.RESERVED ? PendingAction.RELEASE : PendingAction.NONE, now);
        return null;
    }

    private String onPaymentInFlight(PaymentStatus status, Instant now) {
        if (!AWAITING_PAYMENT.contains(orderStatus)) {
            return isClosed() ? ORDER_CLOSED : ALREADY_PAID;
        }
        paymentStatus = status;
        // Extended by the caller through extendExpiry; a payment mid-flight must not be expired under it.
        return null;
    }

    private String onWaitingVerification(Instant now) {
        if (!AWAITING_PAYMENT.contains(orderStatus)) {
            return isClosed() ? ORDER_CLOSED : ALREADY_PAID;
        }
        paymentStatus = PaymentStatus.WAITING_VERIFICATION;
        expiresAt = null; // a merchant is looking at a transfer proof — never auto-expire under them
        return null;
    }

    private String onRefunded(Instant now) {
        if (paymentStatus != PaymentStatus.PAID) {
            return "not paid";
        }
        paymentStatus = PaymentStatus.REFUNDED;
        OrderStatus next = DELIVERED_OR_LATER.contains(orderStatus) ? OrderStatus.RETURNED : OrderStatus.CANCELLED;
        if (!isClosed()) {
            close(next, "payment refunded", null, now);
        }
        if (inventoryStatus == InventoryStatus.RESERVED) {
            pending(PendingAction.RELEASE, now);
        } else if (inventoryStatus == InventoryStatus.COMMITTED) {
            flag("refunded after stock was committed", now);
        }
        return null;
    }

    /**
     * Inventory released the reservation because nobody committed it in time.
     */
    public SignalOutcome applyReservationExpired(String reservationRef, Instant now) {
        String ref = String.format("%s:EXPIRED", reservationRef);
        if (isDuplicate(OrderEventSource.INVENTORY, ref)) {
            return recordSignal(OrderEventType.RESERVATION_EXPIRED, OrderEventSource.INVENTORY, ref,
                    OrderEventOutcome.DUPLICATE, null, now);
        }
        String ignored = null;
        if (inventoryStatus != InventoryStatus.RESERVED) {
            ignored = "nothing reserved";
        } else if (AWAITING_PAYMENT.contains(orderStatus)) {
            inventoryStatus = InventoryStatus.RELEASED;
            paymentStatus = PaymentStatus.EXPIRED;
            close(OrderStatus.CANCELLED, "reservation expired", null, now);
            pending(PendingAction.NONE, now);
        } else if (pendingAction == PendingAction.COMMIT) {
            inventoryStatus = InventoryStatus.RELEASED;
            pending(PendingAction.NONE, now);
            flag("stock released before it could be committed — re-reserve manually", now);
        } else {
            ignored = ORDER_CLOSED;
        }
        return recordSignal(OrderEventType.RESERVATION_EXPIRED, OrderEventSource.INVENTORY, ref,
                ignored == null ? OrderEventOutcome.APPLIED : OrderEventOutcome.IGNORED, ignored, now);
    }

    // ---------------------------------------------------------------- jobs

    /**
     * The expiry job found this order unpaid past {@link #getExpiresAt()} and payment confirmed nothing arrived.
     */
    public void expired(Instant now) {
        if (!AWAITING_PAYMENT.contains(orderStatus)) {
            throw new IllegalStateException(String.format("order %s is %s, not awaiting payment", id, orderStatus));
        }
        paymentStatus = PaymentStatus.EXPIRED;
        close(OrderStatus.CANCELLED, "payment window expired", null, now);
        pending(inventoryStatus == InventoryStatus.RESERVED ? PendingAction.RELEASE : PendingAction.NONE, now);
        record(OrderEventType.EXPIRED, OrderEventSource.JOB, null, OrderEventOutcome.APPLIED, null, now);
    }

    public void extendExpiry(Instant until, Instant now) {
        if (AWAITING_PAYMENT.contains(orderStatus) && (expiresAt == null || expiresAt.isBefore(until))) {
            expiresAt = until;
        }
    }

    public void recoveryAttempted(Instant now) {
        pendingActionAttempts++;
        pendingActionUpdatedAt = now;
        record(OrderEventType.RECOVERY_RETRIED, OrderEventSource.JOB, null, OrderEventOutcome.APPLIED,
                String.format("attempt %d of %s", pendingActionAttempts, pendingAction), now);
    }

    public void recoveryGaveUp(Instant now) {
        flag(String.format("recovery of %s exhausted after %d attempts", pendingAction, pendingActionAttempts), now);
        record(OrderEventType.RECOVERY_GAVE_UP, OrderEventSource.JOB, null, OrderEventOutcome.APPLIED,
                attentionReason, now);
    }

    // ---------------------------------------------------------------- console

    /**
     * Moves the order forward along the fulfilment path, or to RETURNED once delivered. Anything else is a 409.
     */
    public void fulfil(OrderStatus next, String comment, String actor, Instant now)
            throws IllegalOrderTransitionException {
        if (next == OrderStatus.CANCELLED) {
            cancel(comment, actor, now);
            return;
        }
        if (!isLegalFulfilment(next)) {
            throw IllegalOrderTransitionException.of(id, orderStatus, next);
        }
        if (paymentType == PaymentType.COD && next == OrderStatus.DELIVERED && paymentStatus == PaymentStatus.PENDING) {
            paymentStatus = PaymentStatus.PAID; // cash collected at the door
        }
        transition(next, comment, actor, now);
        record(OrderEventType.STATUS_CHANGED, OrderEventSource.CONSOLE, null, OrderEventOutcome.APPLIED, comment, now);
    }

    private boolean isLegalFulfilment(OrderStatus next) {
        return switch (orderStatus) {
            case CONFIRMED -> next == OrderStatus.PROCESSING;
            case PROCESSING -> next == OrderStatus.SHIPPED;
            case SHIPPED -> next == OrderStatus.DELIVERING;
            case DELIVERING -> next == OrderStatus.DELIVERED;
            case DELIVERED -> next == OrderStatus.COMPLETED || next == OrderStatus.RETURNED;
            case COMPLETED -> next == OrderStatus.RETURNED;
            default -> false;
        };
    }

    public void cancel(String comment, String actor, Instant now) throws IllegalOrderTransitionException {
        if (!CANCELLABLE.contains(orderStatus)) {
            throw IllegalOrderTransitionException.of(id, orderStatus, OrderStatus.CANCELLED);
        }
        if (paymentStatus == PaymentStatus.PAID) {
            flag("cancelled after payment — refund required", now);
        } else if (!PAYMENT_LOST.contains(paymentStatus)) {
            paymentStatus = PaymentStatus.CANCELLED;
        }
        close(OrderStatus.CANCELLED, comment, actor, now);
        pending(inventoryStatus == InventoryStatus.RESERVED ? PendingAction.RELEASE : PendingAction.NONE, now);
        record(OrderEventType.CANCELLED, OrderEventSource.CONSOLE, null, OrderEventOutcome.APPLIED, comment, now);
    }

    // ---------------------------------------------------------------- queries

    public boolean isClosed() {
        return CLOSED.contains(orderStatus);
    }

    public boolean isAwaitingPayment() {
        return AWAITING_PAYMENT.contains(orderStatus);
    }

    public boolean isPaymentInFlight() {
        return PAYMENT_IN_FLIGHT.contains(paymentStatus);
    }

    public boolean hasPendingAction() {
        return pendingAction != PendingAction.NONE;
    }

    // ---------------------------------------------------------------- internals

    private void markPaid(Instant now) {
        paymentStatus = PaymentStatus.PAID;
        expiresAt = null;
        transition(OrderStatus.CONFIRMED, null, null, now);
        pending(inventoryStatus == InventoryStatus.RESERVED ? PendingAction.COMMIT : PendingAction.NONE, now);
    }

    private void transition(OrderStatus next, String comment, String actor, Instant now) {
        orderStatus = next;
        history.add(new OrderStatusHistory(this, next, comment, actor, now));
    }

    private void close(OrderStatus terminal, String comment, String actor, Instant now) {
        expiresAt = null;
        transition(terminal, comment, actor, now);
    }

    private void pending(PendingAction action, Instant now) {
        pendingAction = action;
        pendingActionAttempts = 0;
        pendingActionUpdatedAt = now;
    }

    private void flag(String reason, Instant now) {
        needsAttention = true;
        attentionReason = reason;
        history.add(new OrderStatusHistory(this, orderStatus, reason, null, now));
    }

    private void require(boolean condition, OrderEventType step) {
        if (!condition) {
            throw new IllegalStateException(String.format("order %s cannot apply %s while %s/%s/%s pending %s", id,
                    step, orderStatus, paymentStatus, inventoryStatus, pendingAction));
        }
    }

    private boolean isDuplicate(OrderEventSource source, String sourceRef) {
        return events.stream().anyMatch(event -> event.matches(source, sourceRef));
    }

    /**
     * Only an APPLIED row carries the dedup key — a second row with the same key is exactly what the unique index
     * forbids — so a DUPLICATE or IGNORED row keeps the ref in its payload instead.
     */
    private SignalOutcome recordSignal(OrderEventType type, OrderEventSource source, String sourceRef,
                                       OrderEventOutcome outcome, String reason, Instant now) {
        OrderEvent event = new OrderEvent(this, type, source,
                outcome == OrderEventOutcome.APPLIED ? sourceRef : null, outcome, reason, now);
        event.setPayload(sourceRef);
        events.add(event);
        return new SignalOutcome(outcome, orderStatus, paymentStatus);
    }

    private void record(OrderEventType type, OrderEventSource source, String sourceRef, OrderEventOutcome outcome,
                        String reason, Instant now) {
        events.add(new OrderEvent(this, type, source, sourceRef, outcome, reason, now));
    }
}
