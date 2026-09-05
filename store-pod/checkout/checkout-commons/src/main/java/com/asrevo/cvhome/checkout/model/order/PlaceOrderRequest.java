package com.asrevo.cvhome.checkout.model.order;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.asrevo.cvhome.customer.model.customer.PersistableCustomer;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.Getter;
import lombok.Setter;

/**
 * The checkout body: how the shopper pays and who they are. Everything else (lines, prices) comes from the cart and
 * is re-read live at placement.
 */
@Getter
@Setter
public class PlaceOrderRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private PaymentType paymentType;

    @NotNull
    @Valid
    private PersistableCustomer customer;

    private boolean customerAgreement;

    private String comments;
}
