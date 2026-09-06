package com.asrevo.cvhome.checkout.model.order;

import java.io.Serial;
import java.time.Instant;
import java.util.List;

import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.customer.model.customer.address.CustomerAddress;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.Getter;
import lombok.Setter;

/**
 * What {@code POST /cart/{code}/checkout} answers, and the shape of a shopper's own order detail. The storefront
 * navigates to {@code redirectUrl} when present and otherwise shows a success dialog unless {@code orderStatus} is
 * {@code CANCELLED}.
 */
@Getter
@Setter
public class ReadableOrderConfirmation extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String orderRef;

    private CustomerAddress billing;

    private CustomerAddress delivery;

    private String shipping;

    private PaymentType payment;

    private OrderStatus orderStatus;

    private PaymentStatus paymentStatus;

    private String redirectUrl;

    private Instant datePurchased;

    private ReadableTotal total;

    private List<ReadableOrderProduct> products;
}
