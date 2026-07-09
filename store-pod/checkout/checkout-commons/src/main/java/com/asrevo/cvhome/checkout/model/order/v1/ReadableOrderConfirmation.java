package com.asrevo.cvhome.checkout.model.order.v1;

import java.io.Serial;
import java.util.List;

import com.asrevo.cvhome.checkout.model.order.ReadableOrderProduct;
import com.asrevo.cvhome.checkout.model.order.total.ReadableTotal;
import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.customer.model.customer.address.CustomerAddress;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableOrderConfirmation extends Entity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private CustomerAddress billing;

    private CustomerAddress delivery;

    private String shipping;

    private PaymentType payment;

    private ReadableTotal total;

    private List<ReadableOrderProduct> products;

}
