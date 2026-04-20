package com.asrevo.cvhome.checkout.model.order.v0;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.asrevo.cvhome.checkout.model.order.OrderEntity;
import com.asrevo.cvhome.checkout.model.order.PersistableOrderProduct;
import com.asrevo.cvhome.customer.model.customer.PersistableCustomer;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableOrder extends OrderEntity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private PersistableCustomer customer; // might already exist if id > 0, otherwise

    // persist

    private List<PersistableOrderProduct> orderProductItems;

    private boolean shipToBillingAdress = true;

    private boolean shipToDeliveryAddress = false;

}
