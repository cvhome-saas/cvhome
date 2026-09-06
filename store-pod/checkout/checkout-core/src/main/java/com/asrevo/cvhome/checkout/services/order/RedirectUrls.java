package com.asrevo.cvhome.checkout.services.order;

/**
 * Where the payment provider sends the shopper back to. Built by the API from the storefront's origin; the order id
 * is appended once the order exists.
 */
public record RedirectUrls(String success, String cancel) {

    private static final String ORDER_ID_PARAM = "orderId=";

    private static final String QUERY = "?";

    public RedirectUrls withOrderId(Long orderId) {
        return new RedirectUrls(append(success, orderId), append(cancel, orderId));
    }

    private static String append(String url, Long orderId) {
        String separator = url.contains(QUERY) ? "&" : QUERY;
        return String.format("%s%s%s%d", url, separator, ORDER_ID_PARAM, orderId);
    }
}
