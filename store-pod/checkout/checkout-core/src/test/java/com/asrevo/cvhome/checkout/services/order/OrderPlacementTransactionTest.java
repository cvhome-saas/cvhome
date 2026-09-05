package com.asrevo.cvhome.checkout.services.order;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.catalog.model.product.ProductDescription;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.checkout.entity.Cart;
import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.checkout.entity.Orders;
import com.asrevo.cvhome.checkout.errors.CartAlreadyConvertedException;
import com.asrevo.cvhome.checkout.errors.CartEmptyException;
import com.asrevo.cvhome.checkout.errors.CartNotFoundException;
import com.asrevo.cvhome.checkout.errors.CartQuantityOutOfRangeException;
import com.asrevo.cvhome.checkout.errors.ProductNotPurchasableException;
import com.asrevo.cvhome.checkout.model.cart.CartStatus;
import com.asrevo.cvhome.checkout.model.order.PendingAction;
import com.asrevo.cvhome.checkout.model.order.PlaceOrderRequest;
import com.asrevo.cvhome.checkout.repositories.CartRepository;
import com.asrevo.cvhome.checkout.repositories.OrderRepository;
import com.asrevo.cvhome.checkout.services.catalog.ProductSnapshot;
import com.asrevo.cvhome.checkout.services.catalog.ProductSnapshotService;
import com.asrevo.cvhome.checkout.services.customer.CustomerService;
import com.asrevo.cvhome.checkout.services.store.StoreSettings;
import com.asrevo.cvhome.commons.domain.CountryIsoCode;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.customer.model.customer.PersistableCustomer;
import com.asrevo.cvhome.customer.model.customer.address.CustomerAddress;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The first transaction of a placement: the order row from the cart and the live snapshot, the cart frozen, and a
 * resubmit resuming the open order instead of making a second one.
 */
@ExtendWith(MockitoExtension.class)
class OrderPlacementTransactionTest {

    private static final String RUNNER = "Runner";

    private static final String LONDON = "London";

    private static final String LIT_1_00 = "1.00";

    private static final String BATH = "Bath";

    private static final String USD_2 = "USD";

    private static final String EUR_2 = "EUR";

    private static final String GB_2 = "GB";

    private static final CartCode CODE = CartCode.of("cart-1");

    private static final LanguageCode EN = LanguageCode.defaultLanguage();

    private static final ShopperId SHOPPER = new ShopperId("sub-1");

    private static final RedirectUrls URLS = new RedirectUrls(Orders.SUCCESS_URL, Orders.CANCEL_URL);

    @Mock
    private CartRepository carts;

    @Mock
    private OrderRepository orders;

    @Mock
    private CustomerService customers;

    @Mock
    private ProductSnapshotService snapshots;

    @Mock
    private StoreSettings storeSettings;

    private OrderPlacementTransaction placement;

    @BeforeEach
    void setUp() {
        placement = new OrderPlacementTransaction(carts, orders, customers, snapshots, storeSettings,
                Clock.fixed(Orders.T0, ZoneOffset.UTC));
    }

    static PlaceOrderRequest request(PaymentType type) {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setPaymentType(type);
        PersistableCustomer customer = new PersistableCustomer();
        customer.setEmailAddress("shopper@example.com");
        CustomerAddress billing = new CustomerAddress();
        billing.setFirstName("Ada");
        billing.setLastName("Lovelace");
        billing.setAddress("1 Analytical Way");
        billing.setCity(LONDON);
        billing.setCountry(new CountryIsoCode(GB_2));
        customer.setBilling(billing);
        request.setCustomer(customer);
        return request;
    }

    static ProductSnapshot snapshot(String sku, String price, boolean purchasable, int min, int max) {
        ReadableMinimalProduct product = new ReadableMinimalProduct();
        product.setId(1L);
        product.setSku(sku);
        ProductDescription description = new ProductDescription();
        description.setName(RUNNER);
        product.setDescription(description);
        return new ProductSnapshot(sku, product, new BigDecimal(price), new BigDecimal(price), false, purchasable, min,
                max);
    }

    private Cart activeCart() {
        Cart cart = new Cart(Orders.STORE, CODE, EN);
        cart.put(Orders.SKU, 2);
        when(carts.findByStoreMerchantIdAndCode(Orders.STORE, CODE)).thenReturn(Optional.of(cart));
        return cart;
    }

    @Test
    void opensTheOrderFromTheCartAndFreezesTheCart() throws Exception {
        Cart cart = activeCart();
        when(customers.getOrCreate(eq(Orders.STORE), eq(SHOPPER), any())).thenReturn(Orders.customer());
        when(storeSettings.currency(Orders.STORE)).thenReturn(new CurrencyCode(EUR_2));
        when(snapshots.snapshot(eq(Orders.STORE), eq(EN), any()))
                .thenReturn(Map.of(Orders.SKU, snapshot(Orders.SKU, "12.50", true, 1, 0)));
        when(orders.saveAndFlush(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(55L);
            return o;
        });

        Long id = placement.createOrResume(Orders.STORE, EN, CODE, request(PaymentType.STRIPE), SHOPPER, URLS);

        assertThat(id).isEqualTo(55L);
        ArgumentCaptor<Order> saved = ArgumentCaptor.forClass(Order.class);
        verify(orders).saveAndFlush(saved.capture());
        Order order = saved.getValue();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getPendingAction()).isEqualTo(PendingAction.RESERVE);
        assertThat(order.getCurrency().code()).isEqualTo(EUR_2);
        assertThat(order.getTotal()).isEqualByComparingTo("25.00");
        assertThat(order.getLines()).singleElement().satisfies(line -> {
            assertThat(line.getProductName()).isEqualTo(RUNNER);
            assertThat(line.getQuantity()).isEqualTo(2);
        });
        assertThat(order.getBilling().getCountry().isoCode()).isEqualTo(GB_2);
        assertThat(order.getDelivery().getCity()).as("no delivery given → billing").isEqualTo(LONDON);
        assertThat(order.getSuccessUrl()).isEqualTo(Orders.SUCCESS_URL);
        assertThat(cart.getStatus()).isEqualTo(CartStatus.CONVERTED);
        assertThat(cart.getOrderId()).isEqualTo(55L);
        verify(carts).save(cart);
    }

    @Test
    void anExplicitDeliveryAddressIsKept() throws Exception {
        activeCart();
        when(customers.getOrCreate(eq(Orders.STORE), any(), any())).thenReturn(Orders.customer());
        when(storeSettings.currency(Orders.STORE)).thenReturn(new CurrencyCode(USD_2));
        when(snapshots.snapshot(eq(Orders.STORE), eq(EN), any()))
                .thenReturn(Map.of(Orders.SKU, snapshot(Orders.SKU, LIT_1_00, true, 1, 0)));
        when(orders.saveAndFlush(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        PlaceOrderRequest request = request(PaymentType.COD);
        CustomerAddress delivery = new CustomerAddress();
        delivery.setAddress("2 Other Street");
        delivery.setCity(BATH);
        request.getCustomer().setDelivery(delivery);

        placement.createOrResume(Orders.STORE, EN, CODE, request, null, URLS);

        ArgumentCaptor<Order> saved = ArgumentCaptor.forClass(Order.class);
        verify(orders).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getDelivery().getCity()).isEqualTo(BATH);
    }

    @Test
    void anUnknownCartIs404() {
        when(carts.findByStoreMerchantIdAndCode(Orders.STORE, CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placement.createOrResume(Orders.STORE, EN, CODE, request(PaymentType.COD), SHOPPER,
                URLS)).isInstanceOf(CartNotFoundException.class);
    }

    @Test
    void anEmptyCartIsRefused() {
        Cart cart = new Cart(Orders.STORE, CODE, EN);
        when(carts.findByStoreMerchantIdAndCode(Orders.STORE, CODE)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> placement.createOrResume(Orders.STORE, EN, CODE, request(PaymentType.COD), SHOPPER,
                URLS)).isInstanceOf(CartEmptyException.class);
        verify(orders, never()).saveAndFlush(any());
    }

    @Test
    void aConvertedCartWhoseOrderIsOpenResumesThatOrder() throws Exception {
        Cart cart = new Cart(Orders.STORE, CODE, EN);
        cart.convertedInto(100L);
        when(carts.findByStoreMerchantIdAndCode(Orders.STORE, CODE)).thenReturn(Optional.of(cart));
        when(orders.findFirstByStoreMerchantIdAndCartCodeOrderByIdDesc(Orders.STORE, CODE))
                .thenReturn(Optional.of(Orders.reserved(PaymentType.STRIPE)));

        Long id = placement.createOrResume(Orders.STORE, EN, CODE, request(PaymentType.STRIPE), SHOPPER, URLS);

        assertThat(id).isEqualTo(100L);
        verify(orders, never()).saveAndFlush(any());
        verify(customers, never()).getOrCreate(any(), any(), any());
    }

    @Test
    void aConvertedCartWhoseOrderIsClosedIsAConflict() {
        Cart cart = new Cart(Orders.STORE, CODE, EN);
        cart.convertedInto(100L);
        when(carts.findByStoreMerchantIdAndCode(Orders.STORE, CODE)).thenReturn(Optional.of(cart));
        when(orders.findFirstByStoreMerchantIdAndCartCodeOrderByIdDesc(Orders.STORE, CODE))
                .thenReturn(Optional.of(Orders.cancelled(PaymentType.STRIPE)));

        assertThatThrownBy(() -> placement.createOrResume(Orders.STORE, EN, CODE, request(PaymentType.STRIPE), SHOPPER,
                URLS)).isInstanceOf(CartAlreadyConvertedException.class);
    }

    @Test
    void aConvertedCartWithNoOrderRowIsAConflictToo() {
        Cart cart = new Cart(Orders.STORE, CODE, EN);
        cart.convertedInto(100L);
        when(carts.findByStoreMerchantIdAndCode(Orders.STORE, CODE)).thenReturn(Optional.of(cart));
        when(orders.findFirstByStoreMerchantIdAndCartCodeOrderByIdDesc(Orders.STORE, CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placement.createOrResume(Orders.STORE, EN, CODE, request(PaymentType.STRIPE), SHOPPER,
                URLS)).isInstanceOf(CartAlreadyConvertedException.class);
    }

    @Test
    void aLineThatCannotBeBoughtStopsThePlacementBeforeAnyRow() throws Exception {
        activeCart();
        when(customers.getOrCreate(eq(Orders.STORE), any(), any())).thenReturn(Orders.customer());
        when(storeSettings.currency(Orders.STORE)).thenReturn(new CurrencyCode(USD_2));
        when(snapshots.snapshot(eq(Orders.STORE), eq(EN), any()))
                .thenReturn(Map.of(Orders.SKU, snapshot(Orders.SKU, LIT_1_00, false, 1, 0)));

        assertThatThrownBy(() -> placement.createOrResume(Orders.STORE, EN, CODE, request(PaymentType.COD), SHOPPER,
                URLS)).isInstanceOf(ProductNotPurchasableException.class);
        verify(orders, never()).saveAndFlush(any());
    }

    @Test
    void aMissingSkuIsNotPurchasable() throws Exception {
        activeCart();
        when(customers.getOrCreate(eq(Orders.STORE), any(), any())).thenReturn(Orders.customer());
        when(storeSettings.currency(Orders.STORE)).thenReturn(new CurrencyCode(USD_2));
        when(snapshots.snapshot(eq(Orders.STORE), eq(EN), any())).thenReturn(Map.of());

        assertThatThrownBy(() -> placement.createOrResume(Orders.STORE, EN, CODE, request(PaymentType.COD), SHOPPER,
                URLS)).isInstanceOf(ProductNotPurchasableException.class);
    }

    @Test
    void aQuantityOutsideTheSkusBoundsIsRefused() throws Exception {
        activeCart();
        when(customers.getOrCreate(eq(Orders.STORE), any(), any())).thenReturn(Orders.customer());
        when(storeSettings.currency(Orders.STORE)).thenReturn(new CurrencyCode(USD_2));
        when(snapshots.snapshot(eq(Orders.STORE), eq(EN), any()))
                .thenReturn(Map.of(Orders.SKU, snapshot(Orders.SKU, LIT_1_00, true, 1, 1)));

        assertThatThrownBy(() -> placement.createOrResume(Orders.STORE, EN, CODE, request(PaymentType.COD), SHOPPER,
                URLS)).isInstanceOf(CartQuantityOutOfRangeException.class);
    }

    @Test
    void theSkusAskedOfTheSnapshotAreTheCartLines() throws Exception {
        activeCart();
        when(customers.getOrCreate(eq(Orders.STORE), any(), any())).thenReturn(Orders.customer());
        when(storeSettings.currency(Orders.STORE)).thenReturn(new CurrencyCode(USD_2));
        when(snapshots.snapshot(eq(Orders.STORE), eq(EN), eq(List.of(Orders.SKU))))
                .thenReturn(Map.of(Orders.SKU, snapshot(Orders.SKU, LIT_1_00, true, 1, 0)));
        when(orders.saveAndFlush(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        placement.createOrResume(Orders.STORE, EN, CODE, request(PaymentType.COD), SHOPPER, URLS);

        verify(snapshots).snapshot(Orders.STORE, EN, List.of(Orders.SKU));
    }

    @Test
    void theConfirmationIsMappedFromTheStoredOrder() {
        when(orders.findById(100L)).thenReturn(Optional.of(Orders.awaitingPayment(PaymentType.STRIPE)));

        assertThat(placement.confirmation(100L, java.util.Locale.US).getRedirectUrl()).isEqualTo("https://pay/redirect");
        assertThatThrownBy(() -> placement.confirmation(404L, java.util.Locale.US))
                .isInstanceOf(IllegalStateException.class);
    }
}
