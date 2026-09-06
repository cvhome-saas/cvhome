package com.asrevo.cvhome.checkout.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes owned by the checkout context. Every code is a decision a client can act on; failures of the services
 * checkout calls arrive as the typed exceptions of their own {@code -external-api} and are not re-coded here.
 */
public enum CheckoutErrors implements ErrorCode {

    /** No cart with that code in this store — or the cart's order is closed, so the code is spent. */
    CART_NOT_FOUND("CHECKOUT.CART.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /** A checkout was asked for a cart with no lines. */
    CART_EMPTY("CHECKOUT.CART.EMPTY", ErrorCategory.UNPROCESSABLE),

    /** The cart already became an order that is still open; the cart cannot be edited, only its order resumed. */
    CART_ALREADY_CONVERTED("CHECKOUT.CART.ALREADY_CONVERTED", ErrorCategory.CONFLICT),

    /** The sku is unknown to the catalog or unstocked in inventory, so it cannot be put in a cart or an order. */
    PRODUCT_NOT_PURCHASABLE("CHECKOUT.CART.PRODUCT_NOT_PURCHASABLE", ErrorCategory.UNPROCESSABLE),

    /** The quantity is below the sku's minimum or above its maximum per order. */
    CART_QUANTITY_OUT_OF_RANGE("CHECKOUT.CART.QUANTITY_OUT_OF_RANGE", ErrorCategory.UNPROCESSABLE),

    /** No order with that id or ref in this store — or, for a shopper, none of theirs. */
    ORDER_NOT_FOUND("CHECKOUT.ORDER.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /** The store requires a signed-in shopper to place an order and the request carried none. */
    ORDER_LOGIN_REQUIRED("CHECKOUT.ORDER.LOGIN_REQUIRED", ErrorCategory.UNAUTHENTICATED),

    /** The shopper token was minted for another store. */
    ORDER_CLIENT_MISMATCH("CHECKOUT.ORDER.CLIENT_MISMATCH", ErrorCategory.FORBIDDEN),

    /** The requested status change is not legal from the order's current state. */
    ORDER_ILLEGAL_TRANSITION("CHECKOUT.ORDER.ILLEGAL_TRANSITION", ErrorCategory.CONFLICT),

    /** A money amount could not be rendered in the store's currency. */
    PRICE_NOT_FORMATTABLE("CHECKOUT.PRICE.NOT_FORMATTABLE", ErrorCategory.INTERNAL);

    private final String code;

    private final ErrorCategory category;

    CheckoutErrors(String code, ErrorCategory category) {
        this.code = code;
        this.category = category;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public ErrorCategory category() {
        return category;
    }

}
