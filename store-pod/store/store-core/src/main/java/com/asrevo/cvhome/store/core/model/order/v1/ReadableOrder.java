package com.asrevo.cvhome.store.core.model.order.v1;


import com.asrevo.cvhome.store.core.model.customer.ReadableBilling;
import com.asrevo.cvhome.store.core.model.customer.ReadableDelivery;
import com.asrevo.cvhome.store.core.model.order.ReadableOrderProduct;
import com.asrevo.cvhome.store.core.model.order.total.ReadableTotal;
import com.asrevo.cvhome.store.core.model.order.transaction.ReadablePayment;
import com.asrevo.cvhome.store.core.model.shipping.ShippingOption;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ReadableOrder extends Order {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private ReadableBilling billing;
	private ReadableDelivery delivery;
	private ShippingOption shippingOption;
	private ReadablePayment payment;
	private ReadableTotal total;
	private List<ReadableOrderProduct> products;

}
