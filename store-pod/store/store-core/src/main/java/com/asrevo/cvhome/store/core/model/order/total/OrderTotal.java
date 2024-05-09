package com.asrevo.cvhome.store.core.model.order.total;

import com.asrevo.cvhome.commons.domain.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;


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
