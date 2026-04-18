package com.asrevo.cvhome.checkout.model.order.v1;

import java.io.Serial;
import java.util.List;

import com.asrevo.cvhome.checkout.model.order.ReadableOrderProduct;
import com.asrevo.cvhome.checkout.model.order.total.ReadableTotal;
import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.customer.model.customer.address.Address;

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

    private Address billing;

    private Address delivery;

    private String shipping;

    private String payment;

    private ReadableTotal total;

    private List<ReadableOrderProduct> products;

}
