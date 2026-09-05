package com.asrevo.cvhome.checkout.services.order;


import org.springframework.stereotype.Service;

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
import com.asrevo.cvhome.checkout.services.store.StoreSettings;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.errors.UnsupportedCountryCodeException;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiUnavailableException;
import com.asrevo.cvhome.inventory.api.errors.ProductReservationRejectedException;
import com.asrevo.cvhome.payment.api.errors.PaymentApiUnavailableException;
import com.asrevo.cvhome.payment.api.errors.PaymentGatewayRejectedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderPlacementServiceImpl implements OrderPlacementService {

    /** Reserve, initiate payment, and (COD or already paid) commit — the whole placement in one request. */
    private static final int PLACEMENT_STEPS = 3;

    private final OrderPlacementTransaction placement;

    private final StoreSettings storeSettings;

    private final OrderStepRunner steps;

    @Override
    public ReadableOrderConfirmation place(StoreMerchantId store, LanguageCode language, CartCode cartCode,
                                           PlaceOrderRequest request, ShopperId shopper, RedirectUrls redirects)
            throws CartNotFoundException, CartEmptyException, CartAlreadyConvertedException,
            OrderLoginRequiredException, ProductNotPurchasableException, CartQuantityOutOfRangeException,
            UnsupportedCountryCodeException, ProductReservationRejectedException, InventoryApiUnavailableException,
            PaymentGatewayRejectedException, PaymentApiUnavailableException {
        if (shopper == null && storeSettings.requiresLogin(store)) {
            throw OrderLoginRequiredException.of(store.getId());
        }
        Long orderId = placement.createOrResume(store, language, cartCode, request, shopper, redirects);
        steps.runUntilSettled(orderId, PLACEMENT_STEPS);
        return placement.confirmation(orderId, storeSettings.locale(language));
    }
}
