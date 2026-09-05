package com.asrevo.cvhome.checkout;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import com.asrevo.cvhome.checkout.api.CheckoutApiSupport;
import com.asrevo.cvhome.checkout.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.checkout.services.jobs.OrderExpiryJob;
import com.asrevo.cvhome.checkout.services.jobs.OrderRecoveryJob;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiUnavailableException;
import com.asrevo.cvhome.inventory.services.ExternalProductReservationService;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.time.MutableClock;
import com.asrevo.cvhome.testsupport.time.TestClockConfiguration;

import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.ID;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.SHOPPER_A;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.SKU;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.STORE_A;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

/**
 * The two jobs against Postgres with a movable clock: an order stuck mid-placement is finished by recovery once
 * inventory is back, and an unpaid card order past its window is expired — unless payment says it was paid.
 *
 * <p>
 * Its own Spring context (the clock bean differs), so it also proves the service boots with a swapped {@code Clock}.
 * </p>
 */
@ServiceIntegrationTest
@Import({ExternalClientsTestConfiguration.class, TestClockConfiguration.class})
@TestPropertySource(properties = {
        CheckoutApiSupport.POD_PROPERTY,
        "checkout.recovery.interval=1h",
        "checkout.expiry.interval=1h",
        "checkout.recovery.stale-after=1s"})
class OrderJobsIntegrationTest {

    private static final String PENDING_ACTION_ATTEMPTS = "pending_action_attempts";

    private static final String INVENTORY_STATUS = "inventory_status";

    private static final String ADA_EXAMPLE_COM = "ada@example.com";

    private static final String PENDING_PAYMENT_2 = "PENDING_PAYMENT";

    private static final String PENDING_ACTION = "pending_action";

    private static final String PAYMENT_STATUS = "payment_status";

    private static final String ORDER_STATUS = "order_status";

    private static final String STRIPE_2 = "STRIPE";

    private static final String NONE_2 = "NONE";

    private static final String SELECT = """
            select order_status, payment_status, inventory_status, pending_action, needs_attention,
                   pending_action_attempts from checkout.sales_order where order_id = ?
            """;

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private MutableClock clock;

    @Autowired
    private OrderRecoveryJob recovery;

    @Autowired
    private OrderExpiryJob expiry;

    @Autowired
    private ExternalProductReservationService reservations;

    @Autowired
    private ExternalPaymentGatewayService payments;

    private CheckoutApiSupport api;

    @BeforeEach
    void setUp() {
        api = new CheckoutApiSupport(port, signer);
        clock.reset();
    }

    @AfterEach
    void restore() throws Exception {
        ExternalClientsTestConfiguration.reset(reservations, payments);
        clock.reset();
    }

    private Map<String, Object> row(long id) {
        return jdbc.queryForMap(SELECT, id);
    }

    private static RemoteErrorContext outage() {
        return new RemoteErrorContext(null, null, Map.of(), List.of(), "remote", 0, null, new RuntimeException("down"));
    }

    @Test
    void recoveryFinishesAnOrderStuckOnAnInventoryOutage() throws Exception {
        Mockito.doThrow(InventoryApiUnavailableException.from(outage())).when(reservations).reserve(any(), any(), any());
        api.checkout(STORE_A, api.newCart(STORE_A, SKU, 1), api.shopper(STORE_A, SHOPPER_A), STRIPE_2, ADA_EXAMPLE_COM);
        long id = jdbc.queryForObject("select max(order_id) from checkout.sales_order where pending_action = 'RESERVE'",
                Long.class);
        assertThat(row(id)).containsEntry(ORDER_STATUS, "CREATED");

        recovery.recover(); // too fresh: nothing is stale yet
        assertThat(row(id)).containsEntry(PENDING_ACTION_ATTEMPTS, 0);

        clock.advance(Duration.ofSeconds(5));
        recovery.recover(); // inventory still down: attempt counted, order unchanged
        assertThat(row(id)).containsEntry(PENDING_ACTION, "RESERVE").containsEntry(PENDING_ACTION_ATTEMPTS, 1);

        ExternalClientsTestConfiguration.reset(reservations, payments);
        clock.advance(Duration.ofSeconds(5));
        recovery.recover(); // one step: reserve
        assertThat(row(id)).containsEntry(INVENTORY_STATUS, "RESERVED").containsEntry(PENDING_ACTION,
                "INITIATE_PAYMENT");
        clock.advance(Duration.ofSeconds(5));
        recovery.recover(); // next step: payment
        assertThat(row(id)).containsEntry(ORDER_STATUS, PENDING_PAYMENT_2).containsEntry(PENDING_ACTION, NONE_2)
                .containsEntry("needs_attention", false);
        assertThat(jdbc.queryForList("""
                select event_type from checkout.sales_order_event where order_id = ? and event_type = 'RECOVERY_RETRIED'
                """, String.class, id)).hasSize(3);
    }

    @Test
    void expiryClosesAnUnpaidCardOrderButSparesOneThatPaymentSaysWasPaid() throws Exception {
        long expired = api.placed(STORE_A, api.newCart(STORE_A, SKU, 1), api.shopper(STORE_A, SHOPPER_A), STRIPE_2,
                ADA_EXAMPLE_COM).get(ID).asLong();
        long rescued = api.placed(STORE_A, api.newCart(STORE_A, SKU, 1), api.shopper(STORE_A, SHOPPER_A), STRIPE_2,
                ADA_EXAMPLE_COM).get(ID).asLong();
        String rescuedRef = jdbc.queryForObject("select order_ref from checkout.sales_order where order_id = ?",
                String.class, rescued);
        Mockito.doAnswer(invocation -> PaymentResponse.builder().gatewayRef("late-tx")
                .status(rescuedRef.equals(invocation.getArgument(1)) ? PaymentStatus.PAID : PaymentStatus.PENDING).build())
                .when(payments).status(any(), any());

        expiry.expire(); // not due yet
        assertThat(row(expired)).containsEntry(ORDER_STATUS, PENDING_PAYMENT_2);

        clock.advance(Duration.ofMinutes(31));
        expiry.expire();

        assertThat(row(expired)).containsEntry(ORDER_STATUS, "CANCELLED").containsEntry(PAYMENT_STATUS, "EXPIRED")
                .containsEntry(INVENTORY_STATUS, "RELEASED").containsEntry(PENDING_ACTION, NONE_2);
        assertThat(row(rescued)).containsEntry(ORDER_STATUS, "CONFIRMED").containsEntry(PAYMENT_STATUS, "PAID")
                .containsEntry(INVENTORY_STATUS, "COMMITTED");
        assertThat(jdbc.queryForObject("""
                select source_ref from checkout.sales_order_event where order_id = ? and event_type = 'PAYMENT_SIGNAL'
                """, String.class, rescued)).isEqualTo("late-tx:PAID");
    }
}
