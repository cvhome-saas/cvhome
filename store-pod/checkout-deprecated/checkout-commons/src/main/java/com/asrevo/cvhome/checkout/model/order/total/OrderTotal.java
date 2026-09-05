package com.asrevo.cvhome.checkout.model.order.total;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderTotal extends Entity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String title;

    private String text;

    private String code;

    private int order;

    private String module;

    private BigDecimal value;

}
