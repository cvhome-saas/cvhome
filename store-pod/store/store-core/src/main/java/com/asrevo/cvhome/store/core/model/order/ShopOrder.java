package com.asrevo.cvhome.store.core.model.order;

import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.model.order.v0.PersistableOrder;
import com.asrevo.cvhome.store.core.model.shipping.ShippingOption;
import com.asrevo.cvhome.store.core.model.shipping.ShippingSummary;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
 * Orders saved on the website
 * @author Carl Samson
 *
 */
@Setter
@Getter
public class ShopOrder extends PersistableOrder implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<ShoppingCartItem> shoppingCartItems;//overrides parent API list of shoppingcartitem
	private String cartCode = null;

	private OrderTotalSummary orderTotalSummary;//The order total displayed to the end user. That object will be used when committing the order
	
	
	private ShippingSummary shippingSummary;
	private ShippingOption selectedShippingOption = null;//Default selected option
	
	private String defaultPaymentMethodCode = null;
	
	
	private String paymentMethodType = null;//user selected payment type
	private Map<String,String> payment;//user payment information
	
	private String errorMessage = null;


}
