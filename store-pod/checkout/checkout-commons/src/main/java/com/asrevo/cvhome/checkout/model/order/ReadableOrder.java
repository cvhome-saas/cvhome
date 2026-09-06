package com.asrevo.cvhome.checkout.model.order;

import java.io.Serial;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.customer.model.customer.ReadableBilling;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableDelivery;
import com.asrevo.cvhome.store.core.entity.common.InventoryStatus;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.serializer.CurrencyCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.CurrencyCodeSerializer;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;

/**
 * An order for the console and the shopper's order history. The list shape omits {@code products} and
 * {@code customer}; the detail fills them.
 */
@Getter
@Setter
public class ReadableOrder extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String orderRef;

    private OrderStatus orderStatus;

    private PaymentStatus paymentStatus;

    private InventoryStatus inventoryStatus;

    private PaymentType paymentType;

    @JsonSerialize(using = CurrencyCodeSerializer.class)
    @JsonDeserialize(using = CurrencyCodeDeSerializer.class)
    private CurrencyCode currency;

    private Instant datePurchased;

    private ReadableOrderTotal total;

    private List<ReadableOrderTotal> totals = new ArrayList<>();

    private List<ReadableOrderProduct> products;

    private ReadableCustomer customer;

    private ReadableBilling billing;

    private ReadableDelivery delivery;

    private String redirectUrl;

    private boolean needsAttention;

    private String attentionReason;

    private String comments;
}
