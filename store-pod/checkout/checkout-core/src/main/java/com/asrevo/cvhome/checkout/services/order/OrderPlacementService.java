package com.asrevo.cvhome.checkout.services.order;

import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.checkout.errors.CartAlreadyConvertedException;
import com.asrevo.cvhome.checkout.errors.CartEmptyException;
import com.asrevo.cvhome.checkout.errors.CartNotFoundException;
import com.asrevo.cvhome.checkout.errors.CartQuantityOutOfRangeException;
import com.asrevo.cvhome.checkout.errors.OrderLoginRequiredException;
import com.asrevo.cvhome.checkout.errors.ProductNotPurchasableException;
import com.asrevo.cvhome.checkout.model.order.PlaceOrderRequest;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderConfirmation;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.errors.UnsupportedCountryCodeException;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiUnavailableException;
import com.asrevo.cvhome.inventory.api.errors.ProductReservationRejectedException;
import com.asrevo.cvhome.payment.api.errors.PaymentApiUnavailableException;
import com.asrevo.cvhome.payment.api.errors.PaymentGatewayRejectedException;

/**
 * Turns a cart into an order. The order row is committed before the first remote call, so whatever happens afterwards
 * the order exists and is either finished here, resumed by a repeat checkout of the same cart, or finished by
 * {@code OrderRecoveryJob}.
 */
public interface OrderPlacementService {

    ReadableOrderConfirmation place(StoreMerchantId store, LanguageCode language, CartCode cartCode,
                                    PlaceOrderRequest request, ShopperId shopper, RedirectUrls redirects)
            throws CartNotFoundException, CartEmptyException, CartAlreadyConvertedException,
            OrderLoginRequiredException, ProductNotPurchasableException, CartQuantityOutOfRangeException,
            UnsupportedCountryCodeException, ProductReservationRejectedException, InventoryApiUnavailableException,
            PaymentGatewayRejectedException, PaymentApiUnavailableException;
}
