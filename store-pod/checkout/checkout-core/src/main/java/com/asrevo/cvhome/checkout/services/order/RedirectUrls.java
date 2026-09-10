package com.asrevo.cvhome.checkout.services.order;

import com.asrevo.cvhome.checkout.domain.OrderRef;

/**
 * Where the payment provider sends the shopper back to. Built by the API from the storefront's origin; the order id
 * and its reference are appended once the order exists. The id is what the return page displays; the reference is
 * what proves to {@link OrderService#status} that the guest holding the URL is the one who placed the order.
 */
public record RedirectUrls(String success, String cancel) {

    private static final String ORDER_ID_PARAM = "orderId=";

    private static final String REF_PARAM = "&ref=";

    private static final String QUERY = "?";

    public RedirectUrls withOrder(Long orderId, OrderRef ref) {
        return new RedirectUrls(append(success, orderId, ref), append(cancel, orderId, ref));
    }

    private static String append(String url, Long orderId, OrderRef ref) {
        String separator = url.contains(QUERY) ? "&" : QUERY;
        return String.format("%s%s%s%d%s%s", url, separator, ORDER_ID_PARAM, orderId, REF_PARAM, ref.value());
    }
}
