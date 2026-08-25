package com.asrevo.cvhome.checkout.service.facade.checkout;

import java.util.Locale;

import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.errors.OrderNotConvertibleException;
import com.asrevo.cvhome.checkout.errors.OrderProductNotConvertibleException;
import com.asrevo.cvhome.checkout.errors.OrderProductPriceMissingException;
import com.asrevo.cvhome.checkout.errors.ShoppingCartNotFoundException;
import com.asrevo.cvhome.checkout.model.order.v1.PersistableOrder;
import com.asrevo.cvhome.checkout.service.facade.order.model.OrderProcessingResult;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiUnavailableException;
import com.asrevo.cvhome.payment.api.errors.PaymentApiUnavailableException;

public interface OrderPlacementFacade {

    /**
     * Places an order and starts its payment.
     *
     * <p>
     * Two kinds of failure are deliberately <em>not</em> exceptions, because they are answers: a payment the provider
     * refuses, and a reservation catalog refuses. Both resolve the order — cancelled, its reservation released or
     * never taken — and come back in the result.
     * </p>
     *
     * <p>
     * The two unavailable types are the opposite case. Neither payment nor catalog decided anything, so the order is
     * left in place for reconciliation rather than cancelled on a guess. Cancelling on an undecided payment is how an
     * order gets cancelled after being charged; cancelling on an undecided reservation abandons stock catalog may
     * still be holding.
     * </p>
     *
     * @throws PaymentApiUnavailableException      the payment service could not be reached; the payment may or may not
     *                                             have started, and the order is left reserved and pending
     * @throws InventoryApiUnavailableException      catalog could not be reached; the order is left recoverable rather
     *                                             than reported to the shopper as out of stock
     * @throws ShoppingCartNotFoundException       the cart the order refers to no longer exists
     * @throws OrderNotConvertibleException        the submitted payload could not be assembled into an order
     * @throws OrderProductNotConvertibleException a cart line could not be turned into an order line
     * @throws OrderProductPriceMissingException   the catalog returned no price for a cart line
     */
    OrderProcessingResult placeOrder(PersistableOrder order, Customer customer, StoreMerchantId store, LanguageCode language,
                                     Locale locale, String successUrl, String cancelUrl)
            throws PaymentApiUnavailableException, InventoryApiUnavailableException,
            ShoppingCartNotFoundException, OrderNotConvertibleException, OrderProductNotConvertibleException,
            OrderProductPriceMissingException;
}
