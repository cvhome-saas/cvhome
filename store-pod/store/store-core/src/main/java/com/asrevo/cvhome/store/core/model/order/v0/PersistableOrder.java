package com.asrevo.cvhome.store.core.model.order.v0;


import com.asrevo.cvhome.store.core.model.customer.PersistableCustomer;
import com.asrevo.cvhome.store.core.model.order.OrderEntity;
import com.asrevo.cvhome.store.core.model.order.PersistableOrderProduct;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@Deprecated
public class PersistableOrder extends OrderEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private PersistableCustomer customer;//might already exist if id > 0, otherwise persist
    private List<PersistableOrderProduct> orderProductItems;
    private boolean shipToBillingAdress = true;
    private boolean shipToDeliveryAddress = false;


}
