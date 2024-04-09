package com.asrevo.cvhome.store.core.model.order.v0;

import com.asrevo.cvhome.store.core.entity.reference.currency.Currency;
import com.asrevo.cvhome.store.core.model.customer.ReadableBilling;
import com.asrevo.cvhome.store.core.model.customer.ReadableCustomer;
import com.asrevo.cvhome.store.core.model.customer.ReadableDelivery;
import com.asrevo.cvhome.store.core.model.order.OrderEntity;
import com.asrevo.cvhome.store.core.model.order.ReadableOrderProduct;
import com.asrevo.cvhome.store.core.model.order.total.OrderTotal;
import com.asrevo.cvhome.store.core.model.store.ReadableMerchantStore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@Deprecated
public class ReadableOrder extends OrderEntity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private ReadableCustomer customer;
    private List<ReadableOrderProduct> products;
    //public Currency getCurrencyModel() {
    //	return currencyModel;
    //}
    private Currency currencyModel;

    private ReadableBilling billing;
    private ReadableDelivery delivery;
    private ReadableMerchantStore store;


    private OrderTotal total;
    private OrderTotal tax;
    private OrderTotal shipping;

}
