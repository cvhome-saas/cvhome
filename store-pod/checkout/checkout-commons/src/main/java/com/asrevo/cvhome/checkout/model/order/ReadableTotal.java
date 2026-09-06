package com.asrevo.cvhome.checkout.model.order;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * The totals block of an order confirmation: every total line plus the grand total formatted.
 */
@Getter
@Setter
public class ReadableTotal implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigDecimal value;

    private List<ReadableOrderTotal> totals = new ArrayList<>();

    private String grandTotal;
}
