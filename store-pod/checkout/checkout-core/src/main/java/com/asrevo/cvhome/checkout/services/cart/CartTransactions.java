package com.asrevo.cvhome.checkout.services.cart;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.entity.Cart;
import com.asrevo.cvhome.checkout.errors.CartAlreadyConvertedException;
import com.asrevo.cvhome.checkout.errors.CartNotFoundException;
import com.asrevo.cvhome.checkout.repositories.CartRepository;
import com.asrevo.cvhome.checkout.repositories.OrderRepository;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.RequiredArgsConstructor;

/**
 * The cart's reads and writes, each one short transaction, apart from the catalog and inventory calls that price it.
 *
 * <p>
 * Those calls used to run inside the cart's transaction, holding one of checkout's three database connections for as
 * long as catalog took to answer. In the 2026-09-14 spike catalog took seconds, three carts held all three
 * connections, and 199 requests queued behind them until 2,562 failed; a cart update took 194 ms of which 0.7 ms was
 * SQL. {@link CartServiceImpl} now reads the cart here, prices it with no transaction open, and writes here.
 * </p>
 *
 * <p>
 * Every read hands back a cart whose lines are loaded, so it can be priced and mapped after its transaction ends.
 * Every write loads the cart afresh, so a change made between the read and the write is kept, not overwritten.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class CartTransactions {

    private final CartRepository carts;

    private final OrderRepository orders;

    /**
     * A cart that is still worth showing: active, or converted into an order that is still open. Once the order is
     * closed the code is spent and the storefront should start over.
     */
    @Transactional(readOnly = true)
    public Cart open(StoreMerchantId store, CartCode code) throws CartNotFoundException {
        return loaded(findOpen(store, code));
    }

    /** A cart that can still be changed: active, not yet converted into an order. */
    @Transactional(readOnly = true)
    public Cart editable(StoreMerchantId store, CartCode code)
            throws CartNotFoundException, CartAlreadyConvertedException {
        return loaded(findEditable(store, code));
    }

    @Transactional
    public Cart create(Cart cart) {
        return loaded(carts.save(cart));
    }

    /**
     * Applies {@code change} to the cart as it is now, then drops the lines of {@code priced} skus that the snapshot
     * did not return: a shopper is never shown a line the catalog or inventory no longer knows. A line added since the
     * snapshot was taken was not priced and is left as it is.
     */
    @Transactional(rollbackFor = Exception.class)
    public Cart edit(StoreMerchantId store, CartCode code, Consumer<Cart> change, Collection<Sku> priced,
                     Set<Sku> known) throws CartNotFoundException, CartAlreadyConvertedException {
        Cart cart = findEditable(store, code);
        change.accept(cart);
        cart.getLines().removeIf(line -> priced.contains(line.getSku()) && !known.contains(line.getSku()));
        return loaded(carts.save(cart));
    }

    /** Drops lines the catalog or inventory no longer knows from a cart that is still active. */
    @Transactional
    public void prune(StoreMerchantId store, CartCode code, Collection<Sku> unknown) {
        carts.findByStoreMerchantIdAndCode(store, code)
                .filter(Cart::isActive)
                .ifPresent(cart -> {
                    cart.getLines().removeIf(line -> unknown.contains(line.getSku()));
                    carts.save(cart);
                });
    }

    private Cart findOpen(StoreMerchantId store, CartCode code) throws CartNotFoundException {
        Cart cart = carts.findByStoreMerchantIdAndCode(store, code)
                .orElseThrow(() -> CartNotFoundException.of(code, store));
        if (!cart.isActive()) {
            boolean orderOpen = orders.findById(cart.getOrderId()).map(order -> !order.isClosed()).orElse(false);
            if (!orderOpen) {
                throw CartNotFoundException.of(code, store.getId());
            }
        }
        return cart;
    }

    private Cart findEditable(StoreMerchantId store, CartCode code)
            throws CartNotFoundException, CartAlreadyConvertedException {
        Cart cart = findOpen(store, code);
        if (!cart.isActive()) {
            throw CartAlreadyConvertedException.of(code, cart.getOrderId());
        }
        return cart;
    }

    /** Touching the lines loads them while the session is open; the caller prices and maps the cart after it closes. */
    private static Cart loaded(Cart cart) {
        cart.getLines().size();
        return cart;
    }
}
