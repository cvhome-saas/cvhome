package com.asrevo.cvhome.order.model.order.v0;

import com.asrevo.cvhome.customer.model.customer.PersistableCustomer;
import com.asrevo.cvhome.order.model.order.OrderEntity;
import com.asrevo.cvhome.order.model.order.PersistableOrderProduct;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Deprecated
public class PersistableOrder extends OrderEntity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private PersistableCustomer customer; // might already exist if id > 0, otherwise persist
    private List<PersistableOrderProduct> orderProductItems;
    private boolean shipToBillingAdress = true;
    private boolean shipToDeliveryAddress = false;
}
