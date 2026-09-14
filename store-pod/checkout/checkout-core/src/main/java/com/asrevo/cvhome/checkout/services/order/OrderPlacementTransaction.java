package com.asrevo.cvhome.checkout.services.order;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.domain.OrderRef;
import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.checkout.entity.AddressSnapshot;
import com.asrevo.cvhome.checkout.entity.Cart;
import com.asrevo.cvhome.checkout.entity.CartLine;
import com.asrevo.cvhome.checkout.entity.Customer;
import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.checkout.entity.OrderLine;
import com.asrevo.cvhome.checkout.entity.PlacementDraft;
import com.asrevo.cvhome.checkout.errors.CartAlreadyConvertedException;
import com.asrevo.cvhome.checkout.errors.CartEmptyException;
import com.asrevo.cvhome.checkout.errors.CartNotFoundException;
import com.asrevo.cvhome.checkout.errors.CartQuantityOutOfRangeException;
import com.asrevo.cvhome.checkout.errors.ProductNotPurchasableException;
import com.asrevo.cvhome.checkout.model.order.PlaceOrderRequest;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderConfirmation;
import com.asrevo.cvhome.checkout.repositories.CartRepository;
import com.asrevo.cvhome.checkout.repositories.OrderRepository;
import com.asrevo.cvhome.checkout.services.catalog.ProductSnapshot;
import com.asrevo.cvhome.checkout.services.customer.CustomerMapper;
import com.asrevo.cvhome.checkout.services.customer.CustomerService;
import com.asrevo.cvhome.checkout.services.store.StoreSettings;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.errors.UnsupportedCountryCodeException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The first transaction of a placement, in its own bean so the checked exceptions cross the transaction boundary as
 * themselves.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPlacementTransaction {

    private final CartRepository carts;

    private final OrderRepository orders;

    private final CustomerService customers;


    private final StoreSettings storeSettings;

    private final Clock clock;

    /**
     * The skus an active cart holds, for pricing before {@link #createOrResume} opens its transaction; none for a cart
     * that already became an order, which resumes without pricing.
     */
    @Transactional(readOnly = true)
    public List<Sku> skus(StoreMerchantId store, CartCode cartCode) throws CartNotFoundException {
        Cart cart = carts.findByStoreMerchantIdAndCode(store, cartCode)
                .orElseThrow(() -> CartNotFoundException.of(cartCode, store.getId()));
        return cart.isActive() ? cart.getLines().stream().map(CartLine::getSku).toList() : List.of();
    }

    /**
     * The first transaction of a placement. A cart that already became an open order resumes that order rather than
     * creating a second one, so a resubmit after a 502 is safe.
     *
     * <p>
     * {@code snapshot} is the cart priced by catalog and inventory before this transaction opened: pricing inside it held
     * a database connection for as long as those two took to answer. A line priced by nobody — the catalog dropped it,
     * or it was added after the snapshot — is refused, as an unknown sku always was.
     * </p>
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createOrResume(StoreMerchantId store, LanguageCode language, CartCode cartCode,
                                PlaceOrderRequest request, ShopperId shopper, RedirectUrls redirects,
                                Map<Sku, ProductSnapshot> snapshot)
            throws CartNotFoundException, CartEmptyException, CartAlreadyConvertedException,
            ProductNotPurchasableException, CartQuantityOutOfRangeException, UnsupportedCountryCodeException {
        Cart cart = carts.findByStoreMerchantIdAndCode(store, cartCode)
                .orElseThrow(() -> CartNotFoundException.of(cartCode, store.getId()));
        if (!cart.isActive()) {
            Optional<Order> existing = orders.findFirstByStoreMerchantIdAndCartCodeOrderByIdDesc(store, cartCode);
            if (existing.isPresent() && !existing.get().isClosed()) {
                log.info("Cart {} already became order {}; resuming", cartCode, existing.get().getId());
                return existing.get().getId();
            }
            throw CartAlreadyConvertedException.of(cartCode, cart.getOrderId());
        }
        if (cart.getLines().isEmpty()) {
            throw CartEmptyException.of(cartCode);
        }
        Instant now = clock.instant();
        Customer customer = customers.getOrCreate(store, shopper, request.getCustomer());
        AddressSnapshot billing = CustomerMapper.toSnapshot(request.getCustomer().getBilling());
        AddressSnapshot delivery = request.getCustomer().getDelivery() == null || isBlank(request.getCustomer()
                .getDelivery().getAddress()) ? billing : CustomerMapper.toSnapshot(request.getCustomer().getDelivery());

        Order order = Order.place(new PlacementDraft(store, OrderRef.newRef(), cartCode, customer, language,
                storeSettings.currency(store), request.getPaymentType(), billing, delivery, request.getComments()),
                redirects.success(), redirects.cancel(), now);
        addLines(order, cart, snapshot);
        order.computeTotals();
        Order saved = orders.saveAndFlush(order);
        cart.convertedInto(saved.getId());
        carts.save(cart);
        return saved.getId();
    }

    /** The confirmation, read inside a transaction so the lazy lines and totals are there to map. */
    @Transactional(readOnly = true)
    public ReadableOrderConfirmation confirmation(Long orderId, java.util.Locale locale) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new IllegalStateException(String.format("order %d vanished", orderId)));
        return OrderMapper.toConfirmation(order, locale);
    }

    private static void addLines(Order order, Cart cart, Map<Sku, ProductSnapshot> snapshot)
            throws ProductNotPurchasableException, CartQuantityOutOfRangeException {
        for (CartLine line : cart.getLines()) {
            ProductSnapshot product = snapshot.get(line.getSku());
            if (product == null || !product.canBePurchased()) {
                throw ProductNotPurchasableException.of(line.getSku());
            }
            if (!product.allowsQuantity(line.getQuantity())) {
                throw CartQuantityOutOfRangeException.of(line.getSku(), line.getQuantity(),
                        product.quantityOrderMinimum(), product.quantityOrderMaximum());
            }
            OrderLine orderLine = order.addLine(line.getSku(), product.productId(), product.name(),
                    product.finalPrice(), line.getQuantity(), product.imageUrl());
            product.optionLabels().forEach(label -> orderLine.addOption(label.option(), label.value()));
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
