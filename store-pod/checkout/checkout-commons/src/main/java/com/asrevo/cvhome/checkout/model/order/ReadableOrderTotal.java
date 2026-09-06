package com.asrevo.cvhome.checkout.model.order;

import java.io.Serial;
import java.math.BigDecimal;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * One line of an order's (or cart's) totals block: {@code code} is SUBTOTAL / TOTAL (SHIPPING and TAX reserved),
 * {@code value} the number, {@code total} the same amount formatted in the store currency.
 */
@Getter
@Setter
public class ReadableOrderTotal extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String title;

    private String text;

    private String code;

    private int order;

    private String module;

    private BigDecimal value;

    private String total;

    private boolean discounted;
}
