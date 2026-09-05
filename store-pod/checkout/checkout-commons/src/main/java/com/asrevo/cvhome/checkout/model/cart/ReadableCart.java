package com.asrevo.cvhome.checkout.model.cart;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.checkout.model.order.ReadableOrderTotal;
import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * The cart as the storefront stores it in localStorage. Field names are the contract — renaming one empties every
 * live cart.
 */
@Getter
@Setter
public class ReadableCart extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String code;

    private String language;

    private List<ReadableCartItem> products = new ArrayList<>();

    private List<ReadableOrderTotal> totals = new ArrayList<>();

    private BigDecimal subtotal;

    private String displaySubTotal;

    private BigDecimal total;

    private String displayTotal;

    private int quantity;

    /** The order this cart became, once it did. */
    private Long order;

    private String promoCode;

    private Long customer;
}
