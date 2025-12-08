package com.asrevo.cvhome.checkout.entity.order;

import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.checkout.model.order.OrderSummaryType;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * This object is used as input object for many services such as order total calculation
 * and tax calculation
 *
 * @author Carl Samson
 */
@Setter
@Getter
public class OrderSummary implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private OrderSummaryType orderSummaryType = OrderSummaryType.ORDERTOTAL;

	private String promoCode;

	private List<ShoppingCartItem> products = new ArrayList<>();

}
