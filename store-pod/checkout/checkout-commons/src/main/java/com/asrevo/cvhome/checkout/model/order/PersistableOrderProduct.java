package com.asrevo.cvhome.checkout.model.order;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableOrderProduct extends OrderProductEntity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private BigDecimal price; // specify final price

}
