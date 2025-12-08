package com.asrevo.cvhome.checkout.model.shoppingcart;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * Compatible with v1
 *
 * @author c.samson
 */
@Setter
@Getter
public class PersistableShoppingCartItem implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private String product; // or product sku (instance or product)

	private int quantity;

	private String promoCode;

}
