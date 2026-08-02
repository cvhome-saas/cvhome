package com.asrevo.cvhome.checkout.service.facade.checkout;

import java.util.Locale;

import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.model.order.v1.PersistableOrder;
import com.asrevo.cvhome.checkout.service.facade.order.model.OrderProcessingResult;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.api.errors.PaymentApiUnavailableException;
import com.asrevo.cvhome.store.core.exception.ServiceException;

public interface OrderPlacementFacade {

    /**
     * Places an order and starts its payment.
     *
     * <p>
     * A payment the provider <em>refuses</em> is not an exception: the order is cancelled, its reservation released,
     * and the result carries the outcome. {@link PaymentApiUnavailableException} is different in kind — the payment
     * service never answered, so nobody knows whether the payment started. The order is left reserved and pending for
     * reconciliation rather than being cancelled on a guess, and the caller is told the request did not complete.
     * </p>
     *
     * @throws PaymentApiUnavailableException the payment service could not be reached; the order's fate is
     *                                        undetermined and it is deliberately left in place
     */
    OrderProcessingResult placeOrder(PersistableOrder order, Customer customer, StoreMerchantId store, LanguageCode language,
                                     Locale locale, String successUrl, String cancelUrl)
            throws ServiceException, PaymentApiUnavailableException;
}
