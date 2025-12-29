package com.asrevo.cvhome.checkout.model.order.v0;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.model.customer.ReadableBilling;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableDelivery;
import com.asrevo.cvhome.checkout.model.order.OrderEntity;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderProduct;
import com.asrevo.cvhome.checkout.model.order.total.OrderTotal;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableOrder extends OrderEntity implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private ReadableCustomer customer;

	private List<ReadableOrderProduct> products;

	private ReadableBilling billing;

	private ReadableDelivery delivery;

	private StoreMerchantId store;

	private OrderTotal total;

	private OrderTotal tax;

	private OrderTotal shipping;

}
