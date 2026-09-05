package com.asrevo.cvhome.checkout.service.mapper.order;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.catalog.model.product.ProductDescription;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableVariantOptionValue;
import com.asrevo.cvhome.catalog.model.product.ReadableVariantSelection;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProductOption;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.checkout.errors.OrderProductPriceMissingException;
import com.asrevo.cvhome.checkout.model.product.ProductDetails;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.model.SkuPrice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The order line an accepted cart line becomes: the real localized product name (never the placeholder the old
 * populator wrote), the price row, and the sold variant's option/value labels copied so the order survives
 * later catalog edits.
 */
class OrderLineMapperTest {

    private static final String SKU = "SHIRT-RED-L";

    private static final String COLOR = "Color";

    private static final String RED = "Red";

    private static final String SIZE = "Size";

    private static final String LARGE = "L";

    private static final String NAME = "Aurora Shirt";

    private static ShoppingCartItem cartLine() {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setSku(SKU);
        item.setQuantity(2);
        item.setItemPrice(BigDecimal.TEN);
        return item;
    }

    private static ProductDetails details(String name, boolean withVariant) {
        ReadableMinimalProduct product = new ReadableMinimalProduct();
        product.setId(1L);
        product.setSku(SKU);
        if (name != null) {
            ProductDescription description = new ProductDescription();
            description.setName(name);
            product.setDescription(description);
        }
        if (withVariant) {
            ReadableVariantSelection selection = new ReadableVariantSelection();
            selection.setSku(SKU);
            selection.setOptionValues(List.of(
                    new ReadableVariantOptionValue(7L, "color", COLOR, 71L, "red", RED, 0),
                    new ReadableVariantOptionValue(8L, "size", SIZE, 82L, "l", LARGE, 1)));
            product.setVariant(selection);
        }
        SkuInventory inventory = new SkuInventory(SKU, 1L, true, true, 5, 1, 0,
                new SkuPrice(BigDecimal.TEN, BigDecimal.TEN, false, 0, null, null, null));
        return new ProductDetails(product, inventory);
    }

    @Test
    void writesTheRealNameThePriceAndTheOptionSnapshot() throws Exception {
        OrderProduct line = OrderLineMapper.toOrderProduct(cartLine(), details(NAME, true));

        assertThat(line.getProductName()).isEqualTo(NAME);
        assertThat(line.getSku()).isEqualTo(SKU);
        assertThat(line.getProductQuantity()).isEqualTo(2);
        assertThat(line.getPrices()).singleElement()
                .satisfies(price -> assertThat(price.getProductPrice()).isEqualByComparingTo(BigDecimal.TEN));
        assertThat(line.getOrderOptions()).hasSize(2)
                .extracting(OrderProductOption::getOptionName, OrderProductOption::getValueName)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(COLOR, RED),
                        org.assertj.core.groups.Tuple.tuple(SIZE, LARGE));
        assertThat(line.getOrderOptions()).allSatisfy(option ->
                assertThat(option.getOrderProduct()).isSameAs(line));
    }

    @Test
    void aDefaultVariantLineCarriesNoOptionSnapshot() throws Exception {
        OrderProduct line = OrderLineMapper.toOrderProduct(cartLine(), details(NAME, false));
        assertThat(line.getOrderOptions()).isEmpty();
    }

    @Test
    void theSkuIsTheHonestFallbackWhenTheCopyOrProductIsGone() throws Exception {
        assertThat(OrderLineMapper.toOrderProduct(cartLine(), details(null, false)).getProductName())
                .isEqualTo(SKU);

        String longName = "x".repeat(300);
        assertThat(OrderLineMapper.toOrderProduct(cartLine(), details(longName, false)).getProductName())
                .hasSize(255);
    }

    @Test
    void aLineWithoutAPriceRefusesToBook() {
        ShoppingCartItem item = cartLine();
        ReadableMinimalProduct product = new ReadableMinimalProduct();
        product.setSku(SKU);
        ProductDetails unpriced = new ProductDetails(product,
                new SkuInventory(SKU, 1L, false, false, 0, 1, 0, null));

        assertThatThrownBy(() -> OrderLineMapper.toOrderProduct(item, unpriced))
                .isInstanceOf(OrderProductPriceMissingException.class);
        assertThatThrownBy(() -> OrderLineMapper.toOrderProduct(item, null))
                .isInstanceOf(OrderProductPriceMissingException.class);
    }
}
