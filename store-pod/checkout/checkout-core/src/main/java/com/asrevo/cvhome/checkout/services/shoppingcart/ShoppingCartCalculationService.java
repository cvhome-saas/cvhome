/**
 *
 */
package com.asrevo.cvhome.checkout.services.shoppingcart;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.order.OrderTotalSummary;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

/**
 * Interface declaring various methods used to calculate {@link ShoppingCart} object
 * details.
 *
 * @author Umesh Awasthi
 * @since 1.2
 */
public interface ShoppingCartCalculationService {

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
	OrderTotalSummary calculate(final ShoppingCart cartModel, final Customer customer, final StoreMerchantId store,
			final LanguageCode language) throws ServiceException;

	/**
	 * Method which will be used to calculate price for each line items as well Total and
	 * Sub-total for {@link ShoppingCart}.
	 * @param cartModel ShoopingCart mode representing underlying DB object
	 */
	OrderTotalSummary calculate(final ShoppingCart cartModel, final StoreMerchantId store, final LanguageCode language)
			throws ServiceException;

}
