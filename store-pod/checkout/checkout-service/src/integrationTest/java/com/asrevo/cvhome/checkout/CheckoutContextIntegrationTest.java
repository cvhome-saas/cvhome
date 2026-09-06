package com.asrevo.cvhome.checkout;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asrevo.cvhome.checkout.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.checkout.model.order.OrderEventOutcome;
import com.asrevo.cvhome.checkout.model.order.OrderEventSource;
import com.asrevo.cvhome.checkout.model.order.PendingAction;
import com.asrevo.cvhome.store.core.entity.common.InventoryStatus;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The schema and the enums agree. The previous checkout died of exactly this: its DDL accepted five of the ten
 * payment statuses the code wrote, and every expiry failed at flush. Each value of each enum is inserted here.
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class CheckoutContextIntegrationTest {

    private static final String INVENTORY_STATUS = "inventory_status";

    private static final String PENDING_ACTION = "pending_action";

    private static final String PAYMENT_STATUS = "payment_status";

    private static final String ORDER_STATUS = "order_status";

    private static final String PAYMENT_TYPE = "payment_type";

    private static final String S = "%%'%s'%%";

    private static final String LIT_ = "?";

    private static final String STORE = "65f023632bc46470c104b76f";

    private static final String JOIN = "%s%s";

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void contextLoadsAndTheLegacyTablesAreGone() {
        List<String> tables = jdbc.queryForList(
                "select table_name from information_schema.tables where table_schema = 'checkout'", String.class);
        assertThat(tables).contains("sales_order", "sales_order_event", "cart", "customer_account")
                .doesNotContain("orders", "shopping_cart", "customer", "country");
    }

    static Stream<Arguments> enumColumns() {
        Stream<Arguments> order = Stream.of(OrderStatus.values()).map(v -> Arguments.of(ORDER_STATUS, v.name()));
        Stream<Arguments> payment = Stream.of(PaymentStatus.values()).map(v -> Arguments.of(PAYMENT_STATUS, v.name()));
        Stream<Arguments> inventory = Stream.of(InventoryStatus.values())
                .map(v -> Arguments.of(INVENTORY_STATUS, v.name()));
        Stream<Arguments> pending = Stream.of(PendingAction.values()).map(v -> Arguments.of(PENDING_ACTION, v.name()));
        Stream<Arguments> type = Stream.of(PaymentType.values()).map(v -> Arguments.of(PAYMENT_TYPE, v.name()));
        return Stream.of(order, payment, inventory, pending, type).flatMap(s -> s);
    }

    @ParameterizedTest(name = "{0} = {1}")
    @MethodSource("enumColumns")
    void everyEnumValueIsAcceptedByItsCheckConstraint(String column, String value) {
        // Far above anything the table generator will hand out: these rows bypass the sequencer.
        long customerId = 800_000L + Math.abs(String.format(JOIN, column, value).hashCode() % 90_000);
        jdbc.update("""
                insert into checkout.customer_account (customer_id, store_merchant_id, cua_external_id, email)
                values (?, ?, ?, ?)
                """, customerId, STORE, String.format("ctx-%s-%s", column, value), "ctx@example.com");
        long orderId = 900_000L + Math.abs(String.format(JOIN, column, value).hashCode() % 90_000);
        String sql = String.format("""
                insert into checkout.sales_order (order_id, version, store_merchant_id, order_ref, cart_code, customer_id,
                  customer_email, language_code, currency_code, payment_type, order_status, payment_status,
                  inventory_status, pending_action, pending_action_attempts, pending_action_updated_at, needs_attention,
                  success_url, cancel_url, date_purchased, subtotal, total)
                values (?, 0, ?, ?, ?, ?, 'ctx@example.com', 'en', 'USD', %s, %s, %s, %s, %s, 0, ?, false, 's', 'c', ?, 0, 0)
                """,
                PAYMENT_TYPE.equals(column) ? LIT_ : "'COD'",
                ORDER_STATUS.equals(column) ? LIT_ : "'CREATED'",
                PAYMENT_STATUS.equals(column) ? LIT_ : "'PENDING'",
                INVENTORY_STATUS.equals(column) ? LIT_ : "'NOT_REQUESTED'",
                PENDING_ACTION.equals(column) ? LIT_ : "'NONE'");
        Instant now = Instant.now();
        jdbc.update(sql, orderId, STORE, java.util.UUID.randomUUID().toString(), java.util.UUID.randomUUID().toString(),
                customerId, value,
                java.sql.Timestamp.from(now), java.sql.Timestamp.from(now));

        assertThat(jdbc.queryForObject(String.format("select %s from checkout.sales_order where order_id = ?", column),
                String.class, orderId)).isEqualTo(value);
    }

    @Test
    void theEventLedgerAcceptsEverySourceAndOutcome() {
        for (OrderEventSource source : OrderEventSource.values()) {
            for (OrderEventOutcome outcome : OrderEventOutcome.values()) {
                assertThat(jdbc.queryForObject("""
                        select count(*) from information_schema.check_constraints c
                        where c.constraint_name in ('sales_order_event_source_check', 'sales_order_event_outcome_check')
                          and (c.check_clause like ? or c.check_clause like ?)
                        """, Integer.class, String.format(S, source), String.format(S, outcome)))
                        .as("%s / %s listed in a CHECK", source, outcome).isPositive();
            }
        }
    }
}
