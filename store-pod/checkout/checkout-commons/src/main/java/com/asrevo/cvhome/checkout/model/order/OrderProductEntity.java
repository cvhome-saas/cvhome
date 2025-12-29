package com.asrevo.cvhome.checkout.model.order;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderProductEntity extends OrderProduct implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private int orderedQuantity;

	private ReadableMinimalProduct product;

}
