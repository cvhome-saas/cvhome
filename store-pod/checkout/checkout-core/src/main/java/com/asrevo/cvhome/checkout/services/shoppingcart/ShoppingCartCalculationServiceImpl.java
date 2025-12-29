/**
 *
 */
package com.asrevo.cvhome.checkout.services.shoppingcart;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.order.OrderTotalSummary;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.checkout.services.order.OrderService;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * <p>
 * Implementation class responsible for calculating state of shopping cart. This class
 * will take care of calculating price of each line items of shopping cart as well any
 * discount including sub-total and total amount.
 * </p>
 *
 * @author Umesh Awasthi
 * @version 1.2
 */
@Service("shoppingCartCalculationService")
@Slf4j
public class ShoppingCartCalculationServiceImpl implements ShoppingCartCalculationService {

	private final ShoppingCartService shoppingCartService;

	private final OrderService orderService;

	public ShoppingCartCalculationServiceImpl(ShoppingCartService shoppingCartService, OrderService orderService) {
		this.shoppingCartService = shoppingCartService;
		this.orderService = orderService;
	}

	/**
	 * <p>
	 * Method used to recalculate state of shopping cart every time any change has been
	 * made to underlying {@link ShoppingCart} object in DB.
	 * </p>
	 * Following operations will be performed by this method.
	 *
	 * <p>
	 * This method is backbone method for all price calculation related to shopping cart.
	 * </p>
	 */
	@Override
	public OrderTotalSummary calculate(final ShoppingCart cartModel, final Customer customer,
			final StoreMerchantId store, final LanguageCode language) throws ServiceException {

		Assert.notNull(cartModel, "cart cannot be null");
		Assert.notNull(cartModel.getLineItems(), "Cart should have line items.");
		Assert.notNull(store, "Store cannot be null");
		Assert.notNull(customer, "Customer cannot be null");
		OrderTotalSummary orderTotalSummary = orderService.calculateShoppingCartTotal(cartModel, customer, store,
				language);
		updateCartModel(cartModel);
		return orderTotalSummary;
	}

	/**
	 * <p>
	 * Method used to recalculate state of shopping cart every time any change has been
	 * made to underlying {@link ShoppingCart} object in DB.
	 * </p>
	 * Following operations will be performed by this method.
	 *
	 * <p>
	 * This method is backbone method for all price calculation related to shopping cart.
	 * </p>
	 */
	@Override
	public OrderTotalSummary calculate(final ShoppingCart cartModel, final StoreMerchantId store,
			final LanguageCode language) throws ServiceException {

		Assert.notNull(cartModel, "cart cannot be null");
		Assert.notNull(cartModel.getLineItems(), "Cart should have line items.");
		Assert.notNull(store, "Store cannot be null");
		OrderTotalSummary orderTotalSummary = orderService.calculateShoppingCartTotal(cartModel, store, language);
		updateCartModel(cartModel);
		return orderTotalSummary;
	}

	private void updateCartModel(final ShoppingCart cartModel) throws ServiceException {
		shoppingCartService.saveOrUpdate(cartModel);
	}

}
