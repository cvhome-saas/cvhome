package com.asrevo.cvhome.order.model.order.v1;

import com.asrevo.cvhome.commons.domain.Entity;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Order extends Entity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private boolean customerAgreement;
    private String comments;
    private String currency;
}
