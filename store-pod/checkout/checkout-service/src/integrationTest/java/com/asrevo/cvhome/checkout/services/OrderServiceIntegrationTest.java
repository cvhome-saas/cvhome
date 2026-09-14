package com.asrevo.cvhome.checkout.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import com.asrevo.cvhome.checkout.api.CheckoutApiSupport;
import com.asrevo.cvhome.checkout.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.checkout.model.order.OrderFilter;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderList;
import com.asrevo.cvhome.checkout.services.order.OrderService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.sql.SqlStatements;

import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.SKU;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.STORE_A;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the console's order list asks of the database: the page, its count and one batch of totals, however many
 * orders the page holds.
 *
 * <p>
 * It read each row's customer and each row's totals on their own, 42 statements for a page of 20 in the 2026-09-14 load
 * test. Same context as the API tests, so they share one start-up.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class OrderServiceIntegrationTest {

    private static final int ORDERS = 6;

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private OrderService orders;

    @Test
    void aPageOfTheOrderListIsThreeStatementsWhateverItsSize() throws Exception {
        CheckoutApiSupport api = new CheckoutApiSupport(port, signer);
        for (int i = 0; i < ORDERS; i++) {
            api.placed(STORE_A, api.newCart(STORE_A, SKU, 1), null, "COD", String.format("list-%d@example.com", i));
        }

        SqlStatements.Recorded<ReadableOrderList> page = SqlStatements.during(() -> orders.list(
                new StoreMerchantId(STORE_A), LanguageCode.defaultLanguage(), OrderFilter.none(), PageRequest.of(0, 20)));

        assertThat(page.result().getContent()).hasSizeGreaterThanOrEqualTo(ORDERS)
                .allSatisfy(order -> assertThat(order.getTotals()).isNotEmpty());
        assertThat(page.count()).as(page.toString()).isLessThanOrEqualTo(3);
    }
}
