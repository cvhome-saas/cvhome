package com.asrevo.cvhome.checkout.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes owned by the checkout context — carts, orders and the order-placement flow.
 */
public enum CheckoutErrors implements ErrorCode {

    /**
     * No cart exists for that code or id in this store.
     */
    CART_NOT_FOUND("CHECKOUT.CART.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * The product cannot be bought — unpublished, or with no inventory configured at all. A rule about the product,
     * not about the request, hence 422.
     */
    PRODUCT_NOT_PURCHASABLE("CHECKOUT.CART.PRODUCT_NOT_PURCHASABLE", ErrorCategory.UNPROCESSABLE),

    /**
     * No order exists with that id in this store — or, when a customer asked, none belonging to them.
     *
     * <p>
     * A customer reaching for another customer's order gets this rather than a 403 deliberately: answering
     * "forbidden" would confirm the order exists, which is exactly what a caller probing for order ids wants to
     * learn.
     * </p>
     */
    ORDER_NOT_FOUND("CHECKOUT.ORDER.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * The store requires a logged-in shopper to place or track an order, and the request carried no usable
     * authentication.
     */
    ORDER_LOGIN_REQUIRED("CHECKOUT.ORDER.LOGIN_REQUIRED", ErrorCategory.UNAUTHENTICATED),

    /**
     * The token authenticates a shopper of a different store than the one addressed.
     *
     * <p>
     * 403, not the 401 the legacy {@code ServiceRuntimeException} message claimed: the caller <em>is</em>
     * authenticated, and sending them back to log in again would not change the answer.
     * </p>
     */
    ORDER_CLIENT_MISMATCH("CHECKOUT.ORDER.CLIENT_MISMATCH", ErrorCategory.FORBIDDEN),

    /**
     * The customer on the order could neither be found nor created, so there is nobody to place it for.
     */
    ORDER_CUSTOMER_UNRESOLVED("CHECKOUT.ORDER.CUSTOMER_UNRESOLVED", ErrorCategory.UNPROCESSABLE),

    /**
     * A cart line references a product whose price the catalog did not return, so no order line can be priced.
     */
    ORDER_PRODUCT_PRICE_MISSING("CHECKOUT.ORDER_PRODUCT.PRICE_MISSING", ErrorCategory.CONVERSION),

    /**
     * A cart line could not be turned into an order line.
     */
    ORDER_PRODUCT_NOT_CONVERTIBLE("CHECKOUT.ORDER_PRODUCT.NOT_CONVERTIBLE", ErrorCategory.CONVERSION),

    /**
     * An order could not be assembled from the submitted payload.
     */
    ORDER_NOT_CONVERTIBLE("CHECKOUT.ORDER.NOT_CONVERTIBLE", ErrorCategory.CONVERSION),

    /**
     * An amount could not be rendered in the store's currency.
     */
    PRICE_NOT_FORMATTABLE("CHECKOUT.PRICE.NOT_FORMATTABLE", ErrorCategory.CONVERSION);

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
