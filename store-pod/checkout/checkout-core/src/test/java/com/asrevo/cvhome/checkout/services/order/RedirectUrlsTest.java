package com.asrevo.cvhome.checkout.services.order;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.checkout.domain.OrderRef;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The return URL carries the id the page shows and the ref that lets a guest read the status — losing either
 * breaks the payment-return page.
 */
class RedirectUrlsTest {

    private static final OrderRef REF = OrderRef.of("11111111-1111-1111-1111-111111111111");

    @Test
    void appendsTheOrderIdAndTheRefWithTheRightSeparator() {
        RedirectUrls urls = new RedirectUrls("http://shop/en/checkout/success", "http://shop/en/checkout/cancel?x=1")
                .withOrder(42L, REF);

        assertThat(urls.success())
                .isEqualTo("http://shop/en/checkout/success?orderId=42&ref=11111111-1111-1111-1111-111111111111");
        assertThat(urls.cancel())
                .isEqualTo("http://shop/en/checkout/cancel?x=1&orderId=42&ref=11111111-1111-1111-1111-111111111111");
    }
}
