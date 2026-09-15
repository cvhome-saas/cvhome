package com.asrevo.cvhome.checkout.services.order;

import java.util.Map;

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
import com.asrevo.cvhome.checkout.services.catalog.ProductSnapshot;
import com.asrevo.cvhome.checkout.services.catalog.ProductSnapshotService;
import com.asrevo.cvhome.checkout.services.store.StoreSettings;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.Sku;
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

    private final ProductSnapshotService snapshots;

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
        // Priced, and the store's currency read, between two transactions, never inside one: catalog, inventory and
        // merchant may take seconds under load.
        Map<Sku, ProductSnapshot> snapshot = snapshots.snapshot(store, language, placement.skus(store, cartCode));
        CurrencyCode currency = storeSettings.currency(store);
        Long orderId = placement.createOrResume(store, language, cartCode, request, shopper, redirects, snapshot,
                currency);
        try {
            steps.runUntilSettled(orderId, PLACEMENT_STEPS);
        } finally {
            // Reserved or released, inventory's figures for these skus moved: the next cart read on this task asks.
            snapshots.forget(store, snapshot.keySet());
        }
        return placement.confirmation(orderId, storeSettings.locale(language));
    }
}
