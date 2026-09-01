package com.asrevo.cvhome.checkout.errors;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.errors.ErrorCategory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The two refusals a cart line can meet, and why they must not share a class or a code: the storefront picks the
 * shopper's message off the code alone, a caller that wants to retry smaller branches on the type, and "this
 * cannot be bought" and "not this many" call for opposite advice.
 */
class CartRefusalErrorsTest {

    private static final String SKU = "SKU-ZR-CL-DRS02";

    private static final String SKU_PARAM = "sku";

    private static final String MINIMUM = "minimum";

    private static final String MAXIMUM = "maximum";

    private static final String QUANTITY = "quantity";

    @Test
    void bothRefusalsAre422ButAreDistinctTypesWithDistinctCodes() {
        assertThat(CheckoutErrors.PRODUCT_NOT_PURCHASABLE.code())
                .isEqualTo("CHECKOUT.CART.PRODUCT_NOT_PURCHASABLE");
        assertThat(CheckoutErrors.CART_QUANTITY_OUT_OF_RANGE.code())
                .isEqualTo("CHECKOUT.CART.QUANTITY_OUT_OF_RANGE");
        assertThat(CheckoutErrors.PRODUCT_NOT_PURCHASABLE.category()).isEqualTo(ErrorCategory.UNPROCESSABLE);
        assertThat(CheckoutErrors.CART_QUANTITY_OUT_OF_RANGE.category()).isEqualTo(ErrorCategory.UNPROCESSABLE);
        // The split is the point: a caller retrying with a smaller amount catches one and not the other.
        assertThat(CartQuantityOutOfRangeException.of(SKU, 2, 1, 1))
                .isNotInstanceOf(ProductNotPurchasableException.class);
    }

    @Test
    void anUnsellableProductNamesOnlyTheSku() {
        ProductNotPurchasableException failure = ProductNotPurchasableException.of(SKU);

        assertThat(failure.errorCode()).isEqualTo(CheckoutErrors.PRODUCT_NOT_PURCHASABLE);
        assertThat(failure.payload().detail()).contains(SKU);
        assertThat(failure.payload().params()).containsExactly(Map.entry(SKU_PARAM, SKU));
    }

    @Test
    void anOutOfRangeQuantityCarriesTheBoundsTheMessageHasToRender() {
        /*
         * The storefront's message is "You can order between {minimum} and {maximum} of this item —
         * {quantity} isn't allowed", so all four values are contract, not decoration. Without them the
         * shopper is told a number is wrong and never which number would be right.
         */
        CartQuantityOutOfRangeException failure = CartQuantityOutOfRangeException.of(SKU, 2, 1, 1);

        assertThat(failure.errorCode()).isEqualTo(CheckoutErrors.CART_QUANTITY_OUT_OF_RANGE);
        assertThat(failure.payload().params())
                .containsEntry(SKU_PARAM, SKU).containsEntry(QUANTITY, 2)
                .containsEntry(MINIMUM, 1).containsEntry(MAXIMUM, 1);
        assertThat(failure.payload().detail()).contains(SKU).contains("1").contains("2");
    }

    @Test
    void anUnlimitedCeilingReadsAsUnlimitedRatherThanZero() {
        // 0 is the "no limit" sentinel; a detail saying "between 3 and 0" would be nonsense.
        CartQuantityOutOfRangeException failure = CartQuantityOutOfRangeException.of(SKU, 1, 3, 0);

        assertThat(failure.payload().detail()).contains("unlimited");
        assertThat(failure.payload().params()).containsEntry(MAXIMUM, 0);
    }
}
