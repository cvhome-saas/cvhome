package com.asrevo.cvhome.store.core.model.order.v1;

import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.store.core.entity.order.attributes.OrderAttribute;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Order extends Entity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean customerAgreement;
    private String comments;
    private String currency;
    private List<OrderAttribute> attributes = new ArrayList<>();


}
