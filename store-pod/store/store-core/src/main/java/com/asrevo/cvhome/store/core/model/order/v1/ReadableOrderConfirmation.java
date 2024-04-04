package com.asrevo.cvhome.store.core.model.order.v1;

import com.asrevo.cvhome.store.core.model.customer.address.Address;
import com.asrevo.cvhome.store.core.model.entity.Entity;
import com.asrevo.cvhome.store.core.model.order.ReadableOrderProduct;
import com.asrevo.cvhome.store.core.model.order.total.ReadableTotal;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ReadableOrderConfirmation extends Entity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Address billing;
	private Address delivery;
	private String shipping;
	private String payment;
	private ReadableTotal total;
	private List<ReadableOrderProduct> products;

}
