package com.asrevo.cvhome.billing.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.jdbc.repository.query.Query;

import com.asrevo.cvhome.billing.api.v2.BillingStatisticApi;
import com.asrevo.cvhome.billing.repository.SubscriptionAuditRepository;
import com.asrevo.cvhome.billing.repository.SubscriptionInvoiceRepository;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StatisticList;
import com.asrevo.cvhome.commons.domain.StatisticRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The platform's two dated aggregates.
 *
 * <p>
 * Half of this reads the {@code @Query} text itself, which is unusual and deliberate. The two decisions that matter
 * most here live only in SQL — <em>which</em> timestamp the revenue sum is keyed on, and whether the subscription
 * count de-duplicates a returning customer — and each is a one-word edit that changes the meaning of a published
 * business figure without breaking anything a mock could observe. A reviewer would have to notice; this fails
 * instead.
 * </p>
 */
class RevenueStatisticTest {

    private static final String DAY = "2026-08-04";

    private static final String EUR = "EUR";

    private static final String USD = "USD";

    private final SubscriptionInvoiceRepository invoices = mock(SubscriptionInvoiceRepository.class);

    private final SubscriptionAuditRepository audit = mock(SubscriptionAuditRepository.class);

    private final BillingStatisticApi api = new BillingStatisticApi(invoices, audit,
            mock(com.asrevo.cvhome.billing.service.PlatformBillingService.class));

    @Test
    @DisplayName("two currencies come back as two entries rather than one sum")
    void currenciesAreNeverSummedTogether() {
        when(invoices.revenueStatistic(any(), any()))
                .thenReturn(List.of(StatisticEntry.of(DAY, EUR, 2400L),
                        StatisticEntry.of(DAY, USD, 5000L)));

        StatisticList answer = api.revenueStatistic(august());

        // Same day, two entries. Nothing on the platform holds an exchange rate, so a merged 7400 would be a wrong
        // number rather than a missing one — and the currency is the grouping key that keeps them apart.
        assertThat(answer.entries()).hasSize(2);
        assertThat(answer.entries()).extracting(StatisticEntry::name).containsExactly(EUR, USD);
        assertThat(answer.entries()).extracting(StatisticEntry::date).containsOnly(DAY);
    }

    @Test
    @DisplayName("the range is handed to the query as instants at both ends")
    void theRangeReachesTheQuery() {
        when(invoices.revenueStatistic(any(), any())).thenReturn(List.of());

        api.revenueStatistic(august());

        ArgumentCaptor<Instant> from = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> to = ArgumentCaptor.forClass(Instant.class);
        verify(invoices).revenueStatistic(from.capture(), to.capture());
        assertThat(from.getValue()).isEqualTo(Instant.parse("2026-08-01T00:00:00Z"));
        assertThat(to.getValue()).isEqualTo(Instant.parse("2026-09-01T00:00:00Z"));
    }

    @Test
    @DisplayName("revenue counts only settled invoices, keyed on when the money moved")
    void revenueIsKeyedOnPaidAt() {
        String sql = queryOf(SubscriptionInvoiceRepository.class, "revenueStatistic");

        // `settled(...)` writes status, amount_paid and paid_at and does not touch issued_at. Key the sum on
        // issued_at and a *past* day's bar moves when a late payment lands — an operator reloads last month and the
        // chart has changed under them.
        assertThat(sql).contains("date(i.paid_at)");
        assertThat(sql).doesNotContain("issued_at");
        // UNCOLLECTIBLE and VOID fall out of the same predicate, so nothing written off is counted as revenue.
        assertThat(sql).contains("i.status = 'PAID'");
        assertThat(sql).contains("sum(i.amount_paid)");
    }

    @Test
    @DisplayName("a returning customer is not counted as a new subscription")
    void subscriptionsStartedAreDeduplicatedByStore() {
        String sql = queryOf(SubscriptionAuditRepository.class, "subscriptionStatistic");

        // ACTIVATED fires more than once: a suspended store that pays and comes back activates again.
        assertThat(sql).contains("distinct on (a.store_id)");
        // And the range filter has to stay outside the sub-select, or it picks the first row *in the window* rather
        // than the first ever — the same double count with extra steps.
        int subSelectEnd = sql.indexOf("left join billing.plan p");
        assertThat(sql.indexOf("s.occurred_at >= :from")).isGreaterThan(subSelectEnd);
        // A dangling plan id after a catalogue change should be a visible bar, not a total that quietly shrank.
        assertThat(sql).contains("coalesce(p.code, 'UNKNOWN')");
    }

    /** The declared SQL of a repository method, so a change to its meaning is a failing test rather than a review. */
    private static String queryOf(Class<?> repository, String method) {
        for (var candidate : repository.getDeclaredMethods()) {
            if (candidate.getName().equals(method) && candidate.isAnnotationPresent(Query.class)) {
                return candidate.getAnnotation(Query.class).value();
            }
        }
        throw new AssertionError(String.format("%s is not an annotated query on %s", method,
                repository.getSimpleName()));
    }

    private static StatisticRange august() {
        return new StatisticRange(ZonedDateTime.of(2026, 8, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2026, 9, 1, 0, 0, 0, 0, ZoneOffset.UTC));
    }

}
