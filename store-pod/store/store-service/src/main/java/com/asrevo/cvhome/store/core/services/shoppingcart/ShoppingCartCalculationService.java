/**
 *
 */
package com.asrevo.cvhome.store.core.services.shoppingcart;

import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.order.OrderTotalSummary;

/**
 * Interface declaring various methods used to calculate {@link ShoppingCart}
 * object details.
 *
 * @author Umesh Awasthi
 * @since 1.2
 */
public interface ShoppingCartCalculationService {
    /**
     * Method which will be used to calculate price for each line items as well
     * Total and Sub-total for {@link ShoppingCart}.
     *
     * @param cartModel ShoopingCart mode representing underlying DB object
     */
    OrderTotalSummary calculate(
            final ShoppingCart cartModel,
            final Customer customer,
            final MerchantStore store,
            final Language language)
            throws ServiceException;

    /**
     * Method which will be used to calculate price for each line items as well
     * Total and Sub-total for {@link ShoppingCart}.
     *
     * @param cartModel ShoopingCart mode representing underlying DB object
     */
    OrderTotalSummary calculate(
            final ShoppingCart cartModel, final MerchantStore store, final Language language)
            throws ServiceException;
}
