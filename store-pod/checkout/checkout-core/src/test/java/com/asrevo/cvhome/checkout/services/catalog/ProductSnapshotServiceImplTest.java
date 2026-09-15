package com.asrevo.cvhome.checkout.services.catalog;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.catalog.model.product.ReadableCartLineProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableVariantOptionValue;
import com.asrevo.cvhome.catalog.model.product.ReadableVariantSelection;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.checkout.entity.Cart;
import com.asrevo.cvhome.checkout.entity.CartLine;
import com.asrevo.cvhome.checkout.entity.OptionLabel;
import com.asrevo.cvhome.checkout.entity.Orders;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.inventory.model.AvailabilityQuery;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.model.SkuPrice;
import com.asrevo.cvhome.inventory.services.ExternalInventoryService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Catalog and inventory merged by sku; a sku either source does not know is simply absent. A cart's lines are priced
 * from what they remember of the catalogue, and only a line that remembers nothing, or too long ago, asks it again.
 */
@ExtendWith(MockitoExtension.class)
class ProductSnapshotServiceImplTest {

    private static final String HTTP_IMG_1_PNG = "http://img/1.png";

    private static final String ALPHA = "Alpha";

    private static final String COLOR = "Color";

    private static final String LIT_12_00 = "12.00";

    private static final String LIT_9_99 = "9.99";

    private static final String LIT_1_00 = "1.00";

    private static final String BETA = "Beta";

    private static final String SIZE = "size";

    private static final String RED = "Red";

    private static final Sku A_2 = Sku.of("A");

    private static final Sku C_2 = Sku.of("C");

    private static final Sku B_2 = Sku.of("B");

    private static final String V = "V";

    private static final Sku V_2 = Sku.of(V);

    private static final String L = "l";

    private static final LanguageCode EN = LanguageCode.defaultLanguage();

    private static final com.asrevo.cvhome.checkout.domain.CartCode CART = com.asrevo.cvhome.checkout.domain.CartCode.of("c");

    private static final String ALPHA_SLUG = "alpha";

    private static final String BETA_AGAIN = "Beta again";

    private static final Instant NOW = Instant.parse("2026-09-15T00:00:00Z");

    @Mock
    private ExternalProductService products;

    @Mock
    private ExternalInventoryService inventory;

    @Spy
    private Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @InjectMocks
    private ProductSnapshotServiceImpl service;

    static ReadableCartLineProduct product(Sku sku, String name) {
        ReadableCartLineProduct product = new ReadableCartLineProduct();
        product.setProductId(1L);
        product.setSku(sku);
        product.setAvailable(true);
        product.setName(name);
        product.setFriendlyUrl(name == null ? null : name.toLowerCase());
        product.setImageUrl(HTTP_IMG_1_PNG);
        return product;
    }

    static SkuInventory stock(Sku sku, String price, boolean purchasable) {
        return new SkuInventory(sku, 1L, true, purchasable, 5, 1, 3,
                new SkuPrice(new BigDecimal(LIT_12_00), new BigDecimal(price), true, 10, null, null, null));
    }

    @Test
    void mergesBothSourcesAndDropsWhatEitherLacks() {
        when(products.getCartLines(Orders.STORE, List.of(A_2, B_2, C_2), EN))
                .thenReturn(List.of(product(A_2, ALPHA), product(B_2, BETA)));
        when(inventory.queryBySkus(Orders.STORE, new AvailabilityQuery(List.of(A_2, B_2, C_2))))
                .thenReturn(List.of(stock(A_2, LIT_9_99, true), stock(C_2, LIT_1_00, true)));

        Map<Sku, ProductSnapshot> snapshot = service.snapshot(Orders.STORE, EN, List.of(A_2, B_2, C_2, A_2));

        assertThat(snapshot).containsOnlyKeys(A_2);
        ProductSnapshot a = snapshot.get(A_2);
        assertThat(a.name()).isEqualTo(ALPHA);
        assertThat(a.imageUrl()).isEqualTo(HTTP_IMG_1_PNG);
        assertThat(a.productId()).isEqualTo(1L);
        assertThat(a.finalPrice()).isEqualByComparingTo(LIT_9_99);
        assertThat(a.originalPrice()).isEqualByComparingTo(LIT_12_00);
        assertThat(a.discounted()).isTrue();
        assertThat(a.canBePurchased()).isTrue();
        assertThat(a.allowsQuantity(3)).isTrue();
        assertThat(a.allowsQuantity(4)).isFalse();
        assertThat(a.allowsQuantity(0)).isFalse();
        assertThat(a.optionLabels()).isEmpty();
    }

    @Test
    void purchasabilityNeedsCatalogAndInventoryToAgree() {
        ReadableCartLineProduct unavailable = product(A_2, ALPHA);
        unavailable.setAvailable(false);
        when(products.getCartLines(any(), any(), any())).thenReturn(List.of(unavailable, product(B_2, BETA)));
        when(inventory.queryBySkus(any(), any())).thenReturn(List.of(stock(A_2, LIT_1_00, true), stock(B_2, LIT_1_00, false)));

        Map<Sku, ProductSnapshot> snapshot = service.snapshot(Orders.STORE, EN, List.of(A_2, B_2));

        assertThat(snapshot.get(A_2).canBePurchased()).isFalse();
        assertThat(snapshot.get(B_2).canBePurchased()).isFalse();
    }

    @Test
    void variantLabelsFallBackToCodesAndMissingPricesToZero() {
        ReadableCartLineProduct variant = product(V_2, null);
        ReadableVariantSelection selection = new ReadableVariantSelection();
        ReadableVariantOptionValue named = new ReadableVariantOptionValue();
        named.setOptionName(COLOR);
        named.setValueName(RED);
        ReadableVariantOptionValue coded = new ReadableVariantOptionValue();
        coded.setOptionCode(SIZE);
        coded.setValueCode(L);
        selection.setOptionValues(List.of(named, coded));
        variant.setVariant(selection);
        when(products.getCartLines(any(), any(), any())).thenReturn(List.of(variant));
        when(inventory.queryBySkus(any(), any())).thenReturn(List.of(new SkuInventory(V_2, 1L, true, true, 5, 1, 0,
                new SkuPrice(null, null, false, 0, null, null, null))));

        ProductSnapshot v = service.snapshot(Orders.STORE, EN, List.of(V_2)).get(V_2);

        assertThat(v.name()).as("no name: the sku stands in").isEqualTo(V);
        assertThat(v.finalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(v.originalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(v.optionLabels()).containsExactly(new OptionLabel(COLOR, RED), new OptionLabel(SIZE, L));
        assertThat(v.allowsQuantity(99)).as("no maximum").isTrue();
    }

    @Test
    void aLineThatRemembersTheCatalogueIsPricedByInventoryAlone() {
        Cart cart = new Cart(Orders.STORE, CART, EN);
        cart.put(A_2, 1);
        CartLine line = cart.line(A_2).orElseThrow();
        line.remember(7L, ALPHA, ALPHA_SLUG, HTTP_IMG_1_PNG, true, List.of(new OptionLabel(COLOR, RED)), NOW);
        when(inventory.queryBySkus(Orders.STORE, new AvailabilityQuery(List.of(A_2))))
                .thenReturn(List.of(stock(A_2, LIT_9_99, true)));

        Map<Sku, ProductSnapshot> priced = service.priced(Orders.STORE, EN, cart.getLines());

        verify(products, never()).getCartLines(any(), any(), any());
        ProductSnapshot a = priced.get(A_2);
        assertThat(a.productId()).isEqualTo(7L);
        assertThat(a.name()).isEqualTo(ALPHA);
        assertThat(a.product().getFriendlyUrl()).isEqualTo(ALPHA_SLUG);
        assertThat(a.imageUrl()).isEqualTo(HTTP_IMG_1_PNG);
        assertThat(a.optionLabels()).containsExactly(new OptionLabel(COLOR, RED));
        assertThat(a.canBePurchased()).isTrue();
    }

    @Test
    void aLineThatRemembersNothingOrTooLongAgoAsksTheCatalogueAndRemembersTheAnswer() {
        Cart cart = new Cart(Orders.STORE, CART, EN);
        cart.put(A_2, 1);
        cart.put(B_2, 1);
        cart.put(C_2, 1);
        cart.line(A_2).orElseThrow().remember(7L, ALPHA, null, null, true, List.of(), NOW);
        cart.line(B_2).orElseThrow().remember(8L, BETA, null, null, true, List.of(),
                NOW.minus(CartLine.SNAPSHOT_FOR).minus(Duration.ofMinutes(1)));
        when(products.getCartLines(Orders.STORE, List.of(B_2, C_2), EN))
                .thenReturn(List.of(product(B_2, BETA_AGAIN), product(C_2, "Gamma")));
        when(inventory.queryBySkus(Orders.STORE, new AvailabilityQuery(List.of(A_2, B_2, C_2))))
                .thenReturn(List.of(stock(A_2, LIT_1_00, true), stock(B_2, LIT_1_00, true), stock(C_2, LIT_1_00, true)));

        Map<Sku, ProductSnapshot> priced = service.priced(Orders.STORE, EN, cart.getLines());

        assertThat(priced).containsOnlyKeys(A_2, B_2, C_2);
        assertThat(priced.get(B_2).name()).isEqualTo(BETA_AGAIN);
        assertThat(cart.line(B_2).orElseThrow().getProductName()).isEqualTo(BETA_AGAIN);
        assertThat(cart.line(B_2).orElseThrow().getSnapshotAt()).isEqualTo(NOW);
        assertThat(cart.line(C_2).orElseThrow().remembers(NOW)).isTrue();
        assertThat(cart.line(C_2).orElseThrow().getFriendlyUrl()).isEqualTo("gamma");
    }

    @Test
    void nothingIsAskedForNoSkusAndALineRefreshedFromACombinationSkuRemembersItsLabels() {
        assertThat(service.snapshot(Orders.STORE, EN, List.of())).isEmpty();
        assertThat(service.priced(Orders.STORE, EN, List.of())).isEmpty();
        verify(products, never()).getCartLines(any(), any(), any());
        verify(inventory, never()).queryBySkus(any(), any());

        Cart cart = new Cart(Orders.STORE, CART, EN);
        cart.put(V_2, 1);
        ReadableCartLineProduct variant = product(V_2, null);
        ReadableVariantSelection selection = new ReadableVariantSelection();
        ReadableVariantOptionValue coded = new ReadableVariantOptionValue();
        coded.setOptionCode(SIZE);
        coded.setValueCode(L);
        selection.setOptionValues(List.of(coded));
        variant.setVariant(selection);
        when(products.getCartLines(Orders.STORE, List.of(V_2), EN)).thenReturn(List.of(variant));
        when(inventory.queryBySkus(any(), any())).thenReturn(List.of(stock(V_2, LIT_1_00, true)));

        service.priced(Orders.STORE, EN, cart.getLines());

        assertThat(cart.line(V_2).orElseThrow().getOptionLabels()).containsExactly(new OptionLabel(SIZE, L));
    }
}
