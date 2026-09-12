package com.asrevo.cvhome.checkout.services.cart;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.checkout.entity.Cart;
import com.asrevo.cvhome.checkout.entity.CartLine;
import com.asrevo.cvhome.checkout.errors.CartAlreadyConvertedException;
import com.asrevo.cvhome.checkout.errors.CartNotFoundException;
import com.asrevo.cvhome.checkout.errors.CartQuantityOutOfRangeException;
import com.asrevo.cvhome.checkout.errors.ProductNotPurchasableException;
import com.asrevo.cvhome.checkout.model.cart.PersistableCartItem;
import com.asrevo.cvhome.checkout.model.cart.ReadableCart;
import com.asrevo.cvhome.checkout.repositories.CartRepository;
import com.asrevo.cvhome.checkout.repositories.OrderRepository;
import com.asrevo.cvhome.checkout.services.catalog.ProductSnapshot;
import com.asrevo.cvhome.checkout.services.catalog.ProductSnapshotService;
import com.asrevo.cvhome.checkout.services.store.StoreSettings;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository carts;

    private final OrderRepository orders;

    private final ProductSnapshotService snapshots;

    private final StoreSettings storeSettings;

    @Override
    @Transactional
    public ReadableCart create(StoreMerchantId store, LanguageCode language, PersistableCartItem item, ShopperId shopper)
            throws ProductNotPurchasableException, CartQuantityOutOfRangeException {
        Sku sku = skuOf(item);
        Cart cart = new Cart(store, CartCode.newCode(), language);
        cart.setCuaExternalId(shopper == null ? null : shopper.sub());
        Map<Sku, ProductSnapshot> snapshot = snapshots.snapshot(store, language, List.of(sku));
        check(sku, item.getQuantity(), snapshot);
        cart.put(sku, item.getQuantity());
        return CartMapper.toReadable(carts.save(cart), snapshot, storeSettings.currency(store),
                storeSettings.locale(language));
    }

    @Override
    @Transactional
    public ReadableCart upsert(StoreMerchantId store, LanguageCode language, CartCode code, PersistableCartItem item)
            throws CartNotFoundException, CartAlreadyConvertedException, ProductNotPurchasableException,
            CartQuantityOutOfRangeException {
        Sku sku = skuOf(item);
        Cart cart = editable(store, code);
        cart.put(sku, item.getQuantity());
        Map<Sku, ProductSnapshot> snapshot = price(store, language, cart);
        if (item.getQuantity() > 0) {
            check(sku, item.getQuantity(), snapshot);
        }
        return CartMapper.toReadable(carts.save(cart), snapshot, storeSettings.currency(store),
                storeSettings.locale(language));
    }

    @Override
    @Transactional
    public ReadableCart get(StoreMerchantId store, LanguageCode language, CartCode code) throws CartNotFoundException {
        Cart cart = open(store, code);
        Map<Sku, ProductSnapshot> snapshot = price(store, language, cart);
        return CartMapper.toReadable(carts.save(cart), snapshot, storeSettings.currency(store),
                storeSettings.locale(language));
    }

    @Override
    @Transactional
    public ReadableCart removeLine(StoreMerchantId store, LanguageCode language, CartCode code, Sku sku)
            throws CartNotFoundException, CartAlreadyConvertedException {
        Cart cart = editable(store, code);
        cart.remove(sku);
        Map<Sku, ProductSnapshot> snapshot = price(store, language, cart);
        return CartMapper.toReadable(carts.save(cart), snapshot, storeSettings.currency(store),
                storeSettings.locale(language));
    }

    /**
     * A cart that is still worth showing: active, or converted into an order that is still open. Once the order is
     * closed the code is spent and the storefront should start over.
     */
    private Cart open(StoreMerchantId store, CartCode code) throws CartNotFoundException {
        Cart cart = carts.findByStoreMerchantIdAndCode(store, code).orElseThrow(() -> CartNotFoundException.of(code,
                store));
        if (!cart.isActive()) {
            boolean orderOpen = orders.findById(cart.getOrderId()).map(order -> !order.isClosed()).orElse(false);
            if (!orderOpen) {
                throw CartNotFoundException.of(code, store.getId());
            }
        }
        return cart;
    }

    private Cart editable(StoreMerchantId store, CartCode code)
            throws CartNotFoundException, CartAlreadyConvertedException {
        Cart cart = open(store, code);
        if (!cart.isActive()) {
            throw CartAlreadyConvertedException.of(code, cart.getOrderId());
        }
        return cart;
    }

    /**
     * Prices the lines and prunes those the catalog or inventory no longer knows — a shopper is never shown a line
     * they cannot buy.
     */
    private Map<Sku, ProductSnapshot> price(StoreMerchantId store, LanguageCode language, Cart cart) {
        Map<Sku, ProductSnapshot> snapshot = snapshots.snapshot(store, language,
                cart.getLines().stream().map(CartLine::getSku).toList());
        if (cart.isActive()) {
            cart.getLines().removeIf(line -> !snapshot.containsKey(line.getSku()));
        }
        return snapshot;
    }

    /**
     * The line's sku — checked against {@link Sku#FORMAT} by bean validation before the service is called.
     */
    private static Sku skuOf(PersistableCartItem item) {
        return Sku.of(item.getProduct());
    }

    private static void check(Sku sku, int quantity, Map<Sku, ProductSnapshot> snapshot)
            throws ProductNotPurchasableException, CartQuantityOutOfRangeException {
        ProductSnapshot product = snapshot.get(sku);
        if (product == null || !product.canBePurchased()) {
            throw ProductNotPurchasableException.of(sku);
        }
        if (!product.allowsQuantity(quantity)) {
            throw CartQuantityOutOfRangeException.of(sku, quantity, product.quantityOrderMinimum(),
                    product.quantityOrderMaximum());
        }
    }
}
