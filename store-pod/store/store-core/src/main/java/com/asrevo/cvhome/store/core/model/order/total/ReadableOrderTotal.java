package com.asrevo.cvhome.store.core.model.order.total;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class ReadableOrderTotal extends OrderTotal implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String total;
    private boolean discounted;

}
