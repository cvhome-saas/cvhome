package com.asrevo.cvhome.checkout.services.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.catalog.model.product.ProductDescription;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableVariantOptionValue;
import com.asrevo.cvhome.catalog.model.product.ReadableVariantSelection;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.checkout.entity.Orders;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.inventory.model.AvailabilityQuery;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.model.SkuPrice;
import com.asrevo.cvhome.inventory.services.ExternalInventoryService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Catalog and inventory merged by sku; a sku either source does not know is simply absent.
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

    private static final String A_2 = "A";

    private static final String C_2 = "C";

    private static final String B_2 = "B";

    private static final String LIT_0 = "0";

    private static final String V_2 = "V";

    private static final String L = "l";

    private static final LanguageCode EN = LanguageCode.defaultLanguage();

    @Mock
    private ExternalProductService products;

    @Mock
    private ExternalInventoryService inventory;

    @InjectMocks
    private ProductSnapshotServiceImpl service;

    static ReadableMinimalProduct product(String sku, String name) {
        ReadableMinimalProduct product = new ReadableMinimalProduct();
        product.setId(1L);
        product.setSku(sku);
        product.setAvailable(true);
        ProductDescription description = new ProductDescription();
        description.setName(name);
        product.setDescription(description);
        ReadableImage image = new ReadableImage();
        image.setImageUrl(HTTP_IMG_1_PNG);
        product.setImage(image);
        return product;
    }

    static SkuInventory stock(String sku, String price, boolean purchasable) {
        return new SkuInventory(sku, 1L, true, purchasable, 5, 1, 3,
                new SkuPrice(new BigDecimal(LIT_12_00), new BigDecimal(price), true, 10, null, null, null));
    }

    @Test
    void mergesBothSourcesAndDropsWhatEitherLacks() {
        when(products.getDetailedProducts(Orders.STORE, List.of(A_2, B_2, C_2), EN))
                .thenReturn(List.of(product(A_2, ALPHA), product(B_2, BETA)));
        when(inventory.queryBySkus(Orders.STORE, new AvailabilityQuery(List.of(A_2, B_2, C_2))))
                .thenReturn(List.of(stock(A_2, LIT_9_99, true), stock(C_2, LIT_1_00, true)));

        Map<String, ProductSnapshot> snapshot = service.snapshot(Orders.STORE, EN, List.of(A_2, B_2, C_2, A_2));

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
        ReadableMinimalProduct unavailable = product(A_2, ALPHA);
        unavailable.setAvailable(false);
        when(products.getDetailedProducts(any(), any(), any())).thenReturn(List.of(unavailable, product(B_2, BETA)));
        when(inventory.queryBySkus(any(), any())).thenReturn(List.of(stock(A_2, LIT_1_00, true), stock(B_2, LIT_1_00, false)));

        Map<String, ProductSnapshot> snapshot = service.snapshot(Orders.STORE, EN, List.of(A_2, B_2));

        assertThat(snapshot.get(A_2).canBePurchased()).isFalse();
        assertThat(snapshot.get(B_2).canBePurchased()).isFalse();
    }

    @Test
    void variantLabelsFallBackToCodesAndMissingPricesToZero() {
        ReadableMinimalProduct variant = product(V_2, null);
        variant.setDescription(null);
        ReadableVariantSelection selection = new ReadableVariantSelection();
        ReadableVariantOptionValue named = new ReadableVariantOptionValue();
        named.setOptionName(COLOR);
        named.setValueName(RED);
        ReadableVariantOptionValue coded = new ReadableVariantOptionValue();
        coded.setOptionCode(SIZE);
        coded.setValueCode(L);
        selection.setOptionValues(List.of(named, coded));
        variant.setVariant(selection);
        variant.setImage(null);
        when(products.getDetailedProducts(any(), any(), any())).thenReturn(List.of(variant));
        when(inventory.queryBySkus(any(), any())).thenReturn(List.of(new SkuInventory(V_2, 1L, true, true, 5, 0, 0,
                new SkuPrice(null, null, false, 0, null, null, null))));

        ProductSnapshot snapshot = service.snapshot(Orders.STORE, EN, List.of(V_2)).get(V_2);

        assertThat(snapshot.name()).as("no description → the sku").isEqualTo(V_2);
        assertThat(snapshot.imageUrl()).isNull();
        assertThat(snapshot.finalPrice()).isEqualByComparingTo(LIT_0);
        assertThat(snapshot.originalPrice()).isEqualByComparingTo(LIT_0);
        assertThat(snapshot.optionLabels()).extracting(ProductSnapshot.OptionLabel::option, ProductSnapshot.OptionLabel::value)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(COLOR, RED),
                        org.assertj.core.groups.Tuple.tuple(SIZE, L));
        assertThat(snapshot.allowsQuantity(1)).as("min 0 means at least one").isTrue();
        assertThat(snapshot.allowsQuantity(999)).as("max 0 means unbounded").isTrue();
    }

    @Test
    void aSkuWithoutAPriceRowIsNotPurchasable() {
        when(products.getDetailedProducts(any(), any(), any())).thenReturn(List.of(product(A_2, ALPHA)));
        when(inventory.queryBySkus(any(), any())).thenReturn(List.of(new SkuInventory(A_2, 1L, true, true, 5, 1, 0, null)));

        assertThat(service.snapshot(Orders.STORE, EN, List.of(A_2))).isEmpty();
    }

    @Test
    void noSkusMeansNoCalls() {
        assertThat(service.snapshot(Orders.STORE, EN, List.of())).isEmpty();
        verifyNoInteractions(products, inventory);
    }

    @Test
    void duplicateSkusAreAskedOnce() {
        when(products.getDetailedProducts(any(), eq(List.of(A_2)), any())).thenReturn(List.of());
        when(inventory.queryBySkus(any(), any())).thenReturn(List.of());

        service.snapshot(Orders.STORE, EN, List.of(A_2, A_2));

        verify(products).getDetailedProducts(Orders.STORE, List.of(A_2), EN);
    }
}
