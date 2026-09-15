package com.asrevo.cvhome.checkout.services.cart;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

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
import com.asrevo.cvhome.checkout.services.catalog.ProductSnapshot;
import com.asrevo.cvhome.checkout.services.catalog.ProductSnapshotService;
import com.asrevo.cvhome.checkout.services.catalog.ProductSnapshotServiceImpl;
import com.asrevo.cvhome.checkout.services.store.StoreSettings;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.RequiredArgsConstructor;

/**
 * Every operation reads the cart, prices it with no transaction open, then writes in a short transaction of its own
 * ({@link CartTransactions}). No database connection is held while a peer answers. A read or a removal takes the
 * price and stock from the per-sku inventory cache (seconds old at most) and asks the catalogue nothing: what the
 * catalogue said about a sku was kept on its line when it was added ({@link CartLine#remember}). An add asks the
 * catalogue about the sku it adds, and inventory live.
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartTransactions transactions;

    private final ProductSnapshotService snapshots;

    private final StoreSettings storeSettings;

    private final Clock clock;

    @Override
    public ReadableCart create(StoreMerchantId store, LanguageCode language, PersistableCartItem item, ShopperId shopper)
            throws ProductNotPurchasableException, CartQuantityOutOfRangeException {
        Sku sku = skuOf(item);
        Map<Sku, ProductSnapshot> snapshot = snapshots.snapshot(store, language, List.of(sku));
        check(sku, item.getQuantity(), snapshot);
        Cart cart = new Cart(store, CartCode.newCode(), language);
        cart.setCuaExternalId(shopper == null ? null : shopper.sub());
        cart.put(sku, item.getQuantity());
        remember(cart, sku, snapshot);
        return readable(store, language, transactions.create(cart), snapshot);
    }

    @Override
    public ReadableCart upsert(StoreMerchantId store, LanguageCode language, CartCode code, PersistableCartItem item)
            throws CartNotFoundException, CartAlreadyConvertedException, ProductNotPurchasableException,
            CartQuantityOutOfRangeException {
        Sku sku = skuOf(item);
        int quantity = item.getQuantity();
        Cart before = transactions.editable(store, code);
        List<Sku> priced = skusOf(before, sku, quantity > 0);
        Map<Sku, ProductSnapshot> snapshot = new LinkedHashMap<>(pricedWithout(store, language, before, sku));
        if (quantity > 0) {
            snapshot.putAll(snapshots.snapshot(store, language, List.of(sku)));
            check(sku, quantity, snapshot);
        }
        Cart cart = transactions.edit(store, code, c -> {
            c.put(sku, quantity);
            remember(c, sku, snapshot);
        }, priced, snapshot.keySet());
        return readable(store, language, cart, snapshot);
    }

    /**
     * Read-only unless the catalog or inventory dropped one of the cart's skus: then that line is pruned, so the
     * placement that follows does not refuse a line the shopper was never shown.
     */
    @Override
    public ReadableCart get(StoreMerchantId store, LanguageCode language, CartCode code) throws CartNotFoundException {
        Cart cart = transactions.open(store, code);
        Map<Sku, ProductSnapshot> snapshot = snapshots.priced(store, language, cart.getLines());
        List<Sku> unknown = skusOf(cart).stream().filter(sku -> !snapshot.containsKey(sku)).toList();
        if (cart.isActive() && !unknown.isEmpty()) {
            transactions.prune(store, code, unknown);
        }
        return readable(store, language, cart, snapshot);
    }

    @Override
    public ReadableCart removeLine(StoreMerchantId store, LanguageCode language, CartCode code, Sku sku)
            throws CartNotFoundException, CartAlreadyConvertedException {
        Cart before = transactions.editable(store, code);
        List<Sku> priced = skusOf(before, sku, false);
        Map<Sku, ProductSnapshot> snapshot = pricedWithout(store, language, before, sku);
        Cart cart = transactions.edit(store, code, c -> c.remove(sku), priced, snapshot.keySet());
        return readable(store, language, cart, snapshot);
    }

    private ReadableCart readable(StoreMerchantId store, LanguageCode language, Cart cart,
                                  Map<Sku, ProductSnapshot> snapshot) {
        return CartMapper.toReadable(cart, snapshot, storeSettings.currency(store), storeSettings.locale(language));
    }

    /** The cart's other lines priced from their snapshots; nothing is asked when there are none. */
    private Map<Sku, ProductSnapshot> pricedWithout(StoreMerchantId store, LanguageCode language, Cart cart, Sku sku) {
        List<CartLine> others = cart.getLines().stream().filter(line -> !line.getSku().equals(sku)).toList();
        return others.isEmpty() ? Map.of() : snapshots.priced(store, language, others);
    }

    /** Keeps what the catalogue said about {@code sku} on its line, if the cart holds one. */
    private void remember(Cart cart, Sku sku, Map<Sku, ProductSnapshot> snapshot) {
        ProductSnapshot product = snapshot.get(sku);
        if (product != null) {
            cart.line(sku).ifPresent(line -> ProductSnapshotServiceImpl.remember(line, product.product(),
                    clock.instant()));
        }
    }

    private static List<Sku> skusOf(Cart cart) {
        return cart.getLines().stream().map(CartLine::getSku).toList();
    }

    /** The skus the cart will hold once {@code sku} is set ({@code keep}) or removed. */
    private static List<Sku> skusOf(Cart cart, Sku sku, boolean keep) {
        Stream<Sku> others = cart.getLines().stream().map(CartLine::getSku).filter(held -> !held.equals(sku));
        return keep ? Stream.concat(others, Stream.of(sku)).toList() : others.toList();
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
