package com.asrevo.cvhome.checkout.services.order;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedirectUrlsTest {

    @Test
    void appendsTheOrderIdWithTheRightSeparator() {
        RedirectUrls urls = new RedirectUrls("http://shop/en/checkout/success", "http://shop/en/checkout/cancel?x=1")
                .withOrderId(42L);

        assertThat(urls.success()).isEqualTo("http://shop/en/checkout/success?orderId=42");
        assertThat(urls.cancel()).isEqualTo("http://shop/en/checkout/cancel?x=1&orderId=42");
    }
}
