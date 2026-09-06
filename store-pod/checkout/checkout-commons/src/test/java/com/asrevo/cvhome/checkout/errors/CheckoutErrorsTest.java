package com.asrevo.cvhome.checkout.errors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.asrevo.cvhome.errors.ErrorCategory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every code is namespaced under CHECKOUT, and every condition class answers the status its category implies.
 */
class CheckoutErrorsTest {

    private static final String CODE = "c";

    private static final String STORE = "s";

    private static final String SKU = "sku";

    private static final String REF = "ref";

    private static final String SUB = "sub";

    private static final String TOKEN_STORE = "t";

    private static final String FROM = "A";

    private static final String TO = "B";

    private static final String CURRENCY = "XXX";

    @ParameterizedTest
    @EnumSource(CheckoutErrors.class)
    void codesAreNamespacedAndCategorised(CheckoutErrors code) {
        assertThat(code.code()).startsWith("CHECKOUT.");
        assertThat(code.category()).isNotNull();
    }

    @Test
    void eachConditionCarriesItsParamsAndStatus() {
        assertThat(CartNotFoundException.of(CODE, STORE).category()).isEqualTo(ErrorCategory.NOT_FOUND);
        assertThat(CartNotFoundException.of(CODE, STORE).params()).containsEntry("code", CODE);
        assertThat(CartEmptyException.of(CODE).category()).isEqualTo(ErrorCategory.UNPROCESSABLE);
        assertThat(CartAlreadyConvertedException.of(CODE, 5L).category()).isEqualTo(ErrorCategory.CONFLICT);
        assertThat(CartAlreadyConvertedException.of(CODE, 5L).params()).containsEntry("orderId", 5L);
        assertThat(ProductNotPurchasableException.of(SKU).params()).containsEntry(SKU, SKU);
        assertThat(CartQuantityOutOfRangeException.of(SKU, 9, 1, 5).params()).containsEntry("maximum", 5)
                .containsEntry("minimum", 1).containsEntry("quantity", 9);
        assertThat(OrderNotFoundException.of(1L, STORE).category()).isEqualTo(ErrorCategory.NOT_FOUND);
        assertThat(OrderNotFoundException.ofRef(REF, STORE).params()).containsEntry("orderRef", REF);
        assertThat(OrderNotFoundException.forShopper(1L, SUB).params()).containsEntry("shopper", SUB);
        assertThat(OrderLoginRequiredException.of(STORE).category()).isEqualTo(ErrorCategory.UNAUTHENTICATED);
        assertThat(ForeignStoreTokenException.of(STORE, TOKEN_STORE).category()).isEqualTo(ErrorCategory.FORBIDDEN);
        assertThat(ForeignStoreTokenException.of(STORE, TOKEN_STORE).params()).containsEntry("tokenStore", TOKEN_STORE);
        assertThat(IllegalOrderTransitionException.of(1L, FROM, TO).category()).isEqualTo(ErrorCategory.CONFLICT);
        assertThat(IllegalOrderTransitionException.of(1L, FROM, TO).params()).containsEntry("to", TO);
        assertThat(PriceNotFormattableException.of(1, CURRENCY, new RuntimeException()).category())
                .isEqualTo(ErrorCategory.INTERNAL);
        assertThat(PriceNotFormattableException.of(1, CURRENCY, null).getMessage()).contains(CURRENCY);
    }
}
