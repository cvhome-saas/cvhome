package com.asrevo.cvhome.checkout.services.cart;

import java.math.BigDecimal;
import java.util.Locale;
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
import com.asrevo.cvhome.checkout.entity.Orders;
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
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lines are sku + absolute quantity; every read re-prices from the live snapshot and drops lines nobody can buy.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    private static final String LIT_20_00 = "$20.00";

    private static final String LIT_10_00 = "10.00";

    private static final String SUB_1 = "sub-1";

    private static final String LIT_1_00 = "1.00";

    private static final LanguageCode EN = LanguageCode.defaultLanguage();

    private static final CartCode CODE = CartCode.of("cart-1");

    private static final String SKU_A = "SKU-A";

    private static final String SKU_B = "SKU-B";

    @Mock
    private CartRepository carts;

    @Mock
    private OrderRepository orders;

    @Mock
    private ProductSnapshotService snapshots;

    @Mock
    private StoreSettings storeSettings;

    private CartServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CartServiceImpl(carts, orders, snapshots, storeSettings);
        lenient().when(storeSettings.currency(Orders.STORE)).thenReturn(new CurrencyCode("USD"));
        lenient().when(storeSettings.locale(any())).thenReturn(Locale.US);
        lenient().when(carts.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    static ProductSnapshot snapshot(String sku, String price, boolean purchasable, int min, int max) {
        ReadableMinimalProduct product = new ReadableMinimalProduct();
        product.setSku(sku);
        product.setId(3L);
        ProductDescription description = new ProductDescription();
        description.setName(sku.toLowerCase());
        product.setDescription(description);
        return new ProductSnapshot(sku, product, new BigDecimal(price), new BigDecimal(price), false, purchasable, min, max);
    }

    private static PersistableCartItem item(String sku, int quantity) {
        PersistableCartItem item = new PersistableCartItem();
        item.setProduct(sku);
        item.setQuantity(quantity);
        return item;
    }

    private Cart existing() {
        Cart cart = new Cart(Orders.STORE, CODE, EN);
        cart.setId(9L);
        cart.put(SKU_A, 2);
        when(carts.findByStoreMerchantIdAndCode(Orders.STORE, CODE)).thenReturn(Optional.of(cart));
        return cart;
    }

    @Test
    void creatingACartPricesTheFirstLineAndMintsACode() throws Exception {
        when(snapshots.snapshot(eq(Orders.STORE), eq(EN), any()))
                .thenReturn(Map.of(SKU_A, snapshot(SKU_A, LIT_10_00, true, 1, 0)));

        ReadableCart cart = service.create(Orders.STORE, EN, item(SKU_A, 2), new ShopperId(SUB_1));

        assertThat(cart.getCode()).isNotBlank();
        assertThat(cart.getLanguage()).isEqualTo("en");
        assertThat(cart.getQuantity()).isEqualTo(2);
        assertThat(cart.getSubtotal()).isEqualByComparingTo("20.00");
        assertThat(cart.getDisplaySubTotal()).isEqualTo(LIT_20_00);
        assertThat(cart.getDisplayTotal()).isEqualTo(LIT_20_00);
        assertThat(cart.getTotals()).extracting("code", "total").containsExactly(
                org.assertj.core.groups.Tuple.tuple("SUBTOTAL", LIT_20_00),
                org.assertj.core.groups.Tuple.tuple("TOTAL", LIT_20_00));
        assertThat(cart.getProducts()).singleElement().satisfies(line -> {
            assertThat(line.getSku()).isEqualTo(SKU_A);
            assertThat(line.getDescription().getName()).isEqualTo("sku-a");
            assertThat(line.getFinalPrice()).isEqualTo("$10.00");
            assertThat(line.getDisplaySubTotal()).isEqualTo(LIT_20_00);
            assertThat(line.isAvailable()).isTrue();
        });
        ArgumentCaptor<Cart> saved = ArgumentCaptor.forClass(Cart.class);
        verify(carts).save(saved.capture());
        assertThat(saved.getValue().getCuaExternalId()).isEqualTo(SUB_1);
    }

    @Test
    void upsertSetsTheAbsoluteQuantityAndZeroRemoves() throws Exception {
        Cart cart = existing();
        when(snapshots.snapshot(eq(Orders.STORE), eq(EN), any())).thenReturn(Map.of(
                SKU_A, snapshot(SKU_A, LIT_10_00, true, 1, 0), SKU_B, snapshot(SKU_B, "5.00", true, 1, 0)));

        ReadableCart afterAdd = service.upsert(Orders.STORE, EN, CODE, item(SKU_B, 3));
        assertThat(afterAdd.getQuantity()).isEqualTo(5);
        assertThat(afterAdd.getTotal()).isEqualByComparingTo("35.00");

        ReadableCart afterSet = service.upsert(Orders.STORE, EN, CODE, item(SKU_A, 1));
        assertThat(cart.line(SKU_A)).get().satisfies(line -> assertThat(line.getQuantity()).isEqualTo(1));
        assertThat(afterSet.getTotal()).isEqualByComparingTo("25.00");

        ReadableCart afterRemove = service.upsert(Orders.STORE, EN, CODE, item(SKU_B, 0));
        assertThat(afterRemove.getProducts()).hasSize(1);
        assertThat(cart.line(SKU_B)).isEmpty();
    }

    @Test
    void readingACartPrunesLinesNobodyCanBuyAnyMore() throws Exception {
        Cart cart = existing();
        cart.put(SKU_B, 1);
        when(snapshots.snapshot(eq(Orders.STORE), eq(EN), any()))
                .thenReturn(Map.of(SKU_A, snapshot(SKU_A, LIT_10_00, true, 1, 0)));

        ReadableCart readable = service.get(Orders.STORE, EN, CODE);

        assertThat(readable.getProducts()).hasSize(1);
        assertThat(cart.getLines()).hasSize(1);
        verify(carts).save(cart);
    }

    @Test
    void aLineFlaggedNotPurchasableStaysButShowsUnavailable() throws Exception {
        existing();
        when(snapshots.snapshot(eq(Orders.STORE), eq(EN), any()))
                .thenReturn(Map.of(SKU_A, snapshot(SKU_A, LIT_10_00, false, 1, 0)));

        ReadableCart readable = service.get(Orders.STORE, EN, CODE);

        assertThat(readable.getProducts()).singleElement().satisfies(line -> assertThat(line.isAvailable()).isFalse());
    }

    @Test
    void removeLineDropsTheSkuAndAnswersTheRest() throws Exception {
        Cart cart = existing();
        cart.put(SKU_B, 1);
        when(snapshots.snapshot(eq(Orders.STORE), eq(EN), any()))
                .thenReturn(Map.of(SKU_A, snapshot(SKU_A, LIT_10_00, true, 1, 0)));

        ReadableCart readable = service.removeLine(Orders.STORE, EN, CODE, SKU_B);

        assertThat(readable.getProducts()).singleElement().satisfies(line -> assertThat(line.getSku()).isEqualTo(SKU_A));
        assertThat(cart.line(SKU_B)).isEmpty();
    }

    @Test
    void anUnknownSkuOrAnUnpurchasableOneIsRefused() {
        when(snapshots.snapshot(eq(Orders.STORE), eq(EN), any())).thenReturn(Map.of());
        assertThatThrownBy(() -> service.create(Orders.STORE, EN, item(SKU_A, 1), null))
                .isInstanceOf(ProductNotPurchasableException.class);

        existing();
        when(snapshots.snapshot(eq(Orders.STORE), eq(EN), any()))
                .thenReturn(Map.of(SKU_A, snapshot(SKU_A, LIT_1_00, false, 1, 0)));
        assertThatThrownBy(() -> service.upsert(Orders.STORE, EN, CODE, item(SKU_A, 1)))
                .isInstanceOf(ProductNotPurchasableException.class);
    }

    @Test
    void aQuantityOutsideTheBoundsIsRefusedWithTheBounds() {
        when(snapshots.snapshot(eq(Orders.STORE), eq(EN), any()))
                .thenReturn(Map.of(SKU_A, snapshot(SKU_A, LIT_1_00, true, 2, 4)));

        assertThatThrownBy(() -> service.create(Orders.STORE, EN, item(SKU_A, 5), null))
                .isInstanceOf(CartQuantityOutOfRangeException.class)
                .satisfies(e -> assertThat(((CartQuantityOutOfRangeException) e).params()).containsEntry("maximum", 4));
        assertThatThrownBy(() -> service.create(Orders.STORE, EN, item(SKU_A, 1), null))
                .isInstanceOf(CartQuantityOutOfRangeException.class);
    }

    @Test
    void anUnknownCodeIs404() {
        when(carts.findByStoreMerchantIdAndCode(Orders.STORE, CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(Orders.STORE, EN, CODE)).isInstanceOf(CartNotFoundException.class);
        assertThatThrownBy(() -> service.removeLine(Orders.STORE, EN, CODE, SKU_A))
                .isInstanceOf(CartNotFoundException.class);
    }

    @Test
    void aConvertedCartWithAnOpenOrderIsReadOnly() throws Exception {
        Cart cart = existing();
        cart.convertedInto(100L);
        when(orders.findById(100L)).thenReturn(Optional.of(Orders.awaitingPayment(PaymentType.STRIPE)));
        when(snapshots.snapshot(eq(Orders.STORE), eq(EN), any()))
                .thenReturn(Map.of(SKU_A, snapshot(SKU_A, LIT_10_00, true, 1, 0)));

        ReadableCart readable = service.get(Orders.STORE, EN, CODE);
        assertThat(readable.getOrder()).isEqualTo(100L);
        assertThat(readable.getProducts()).hasSize(1);

        assertThatThrownBy(() -> service.upsert(Orders.STORE, EN, CODE, item(SKU_A, 3)))
                .isInstanceOf(CartAlreadyConvertedException.class);
        assertThatThrownBy(() -> service.removeLine(Orders.STORE, EN, CODE, SKU_A))
                .isInstanceOf(CartAlreadyConvertedException.class);
    }

    @Test
    void aConvertedCartDoesNotPruneItsFrozenLines() throws Exception {
        Cart cart = existing();
        cart.convertedInto(100L);
        when(orders.findById(100L)).thenReturn(Optional.of(Orders.awaitingPayment(PaymentType.STRIPE)));
        when(snapshots.snapshot(eq(Orders.STORE), eq(EN), any())).thenReturn(Map.of());

        ReadableCart readable = service.get(Orders.STORE, EN, CODE);

        assertThat(readable.getProducts()).isEmpty();
        assertThat(cart.getLines()).as("the order's source is left intact").hasSize(1);
    }

    @Test
    void aConvertedCartWhoseOrderClosedIsSpent() {
        Cart cart = existing();
        cart.convertedInto(100L);
        when(orders.findById(100L)).thenReturn(Optional.of(Orders.cancelled(PaymentType.STRIPE)));

        assertThatThrownBy(() -> service.get(Orders.STORE, EN, CODE)).isInstanceOf(CartNotFoundException.class);
        verify(snapshots, never()).snapshot(any(), any(), any());

        when(orders.findById(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(Orders.STORE, EN, CODE)).isInstanceOf(CartNotFoundException.class);
    }
}
