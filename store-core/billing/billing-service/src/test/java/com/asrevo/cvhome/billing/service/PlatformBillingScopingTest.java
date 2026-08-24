package com.asrevo.cvhome.billing.service;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.billing.commons.InvoiceStatus;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.admin.ListInvoiceQuery;
import com.asrevo.cvhome.billing.commons.dto.admin.ListSubscriptionQuery;
import com.asrevo.cvhome.billing.commons.dto.admin.PlatformInvoiceView;
import com.asrevo.cvhome.billing.commons.dto.admin.PlatformSubscriptionView;
import com.asrevo.cvhome.billing.mappers.PlatformBillingMappers;
import com.asrevo.cvhome.billing.repository.ProcessedStripeEventRepository;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.billing.repository.StripeRequestRepository;
import com.asrevo.cvhome.billing.repository.SubscriptionInvoiceRepository;
import com.asrevo.cvhome.billing.repository.projection.InvoiceTotalRow;
import com.asrevo.cvhome.billing.repository.projection.PlatformInvoiceRow;
import com.asrevo.cvhome.billing.repository.projection.PlatformSubscriptionRow;
import com.asrevo.cvhome.billing.service.impl.PlatformBillingServiceImpl;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * What actually reaches the platform's queries, and what comes back as a page.
 *
 * <p>
 * Every filter on these listings is optional and every one of them is written as
 * {@code cast(:x as varchar) is null or col = :x} — so "the filter was not applied" and "the filter was applied as
 * null" are the same SQL and cannot be told apart by reading the result. These are the checks that say which one the
 * service asked for.
 * </p>
 *
 * <p>
 * The paging is assembled by hand, because Spring Data JDBC has no {@code countQuery}. That means the total is a
 * <em>second</em> query and nothing but a test stops it drifting from the rows: a page whose total came from the
 * row count would report the platform as fifty subscriptions wide forever.
 * </p>
 */
@Tag("unit-test")
class PlatformBillingScopingTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final StoreMerchantId STORE = new StoreMerchantId("507f1f77bcf86cd799439011");

    private static final String EUR_CODE = "EUR";

    private static final String USD_CODE = "USD";

    private static final CurrencyCode EUR = new CurrencyCode(EUR_CODE);

    private static final CurrencyCode USD = new CurrencyCode(USD_CODE);

    /** The plan the register is filtered by, and the one the fixture row sits on. */
    private static final String PRO = "PRO";

    private static final Instant AUGUST = Instant.parse("2026-08-01T00:00:00Z");

    private StoreSubscriptionRepository subscriptions;

    private SubscriptionInvoiceRepository invoices;

    private PlatformBillingServiceImpl service;

    @BeforeEach
    void setUp() {
        subscriptions = mock(StoreSubscriptionRepository.class);
        invoices = mock(SubscriptionInvoiceRepository.class);
        service = new PlatformBillingServiceImpl(subscriptions, invoices, mock(SubscriptionAuditService.class),
                mock(ProcessedStripeEventRepository.class), mock(StripeRequestRepository.class),
                new PlatformBillingMappers());
    }

    /* --------------------------------------------------------------------------- the register ---- */

    @Test
    @DisplayName("every subscription filter reaches the query")
    void subscriptionFiltersReachTheQuery() {
        givenNoSubscriptions();

        service.subscriptions(new ListSubscriptionQuery(ORG, SubscriptionStatus.PAST_DUE, PRO, " 507f ", true),
                PageRequest.of(0, 20));

        ArgumentCaptor<String> org = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> status = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> plan = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> term = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> blocked = ArgumentCaptor.forClass(Boolean.class);
        verify(subscriptions).findVisible(org.capture(), status.capture(), plan.capture(), term.capture(),
                blocked.capture(), anyInt(), anyLong());

        assertThat(org.getValue()).isEqualTo(ORG.id().toString());
        assertThat(status.getValue()).isEqualTo("PAST_DUE");
        assertThat(plan.getValue()).isEqualTo(PRO);
        // Trimmed: the query wraps it in `ilike '%' || :term || '%'`, so the spaces would be searched for.
        assertThat(term.getValue()).isEqualTo("507f");
        assertThat(blocked.getValue()).isTrue();
    }

    @Test
    @DisplayName("an unset filter drops the predicate rather than narrowing to nothing")
    void unsetSubscriptionFiltersAreNull() {
        givenNoSubscriptions();

        service.subscriptions(new ListSubscriptionQuery(null, null, null, "   ", false), PageRequest.of(0, 20));

        ArgumentCaptor<String> org = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> term = ArgumentCaptor.forClass(String.class);
        verify(subscriptions).findVisible(org.capture(), any(), any(), term.capture(), anyBoolean(), anyInt(),
                anyLong());

        assertThat(org.getValue()).isNull();
        // A cleared box and an untouched one are one case; `ilike '%%'` matching everything is the right answer by
        // accident, and relying on that would make them two code paths.
        assertThat(term.getValue()).isNull();
    }

    @Test
    @DisplayName("a null query body is the whole platform, not a failure")
    void aNullQueryIsEveryRow() {
        givenNoSubscriptions();

        service.subscriptions(null, PageRequest.of(0, 20));

        ArgumentCaptor<Boolean> blocked = ArgumentCaptor.forClass(Boolean.class);
        verify(subscriptions).findVisible(any(), any(), any(), any(), blocked.capture(), anyInt(), anyLong());
        assertThat(blocked.getValue()).isFalse();
    }

    @Test
    @DisplayName("the register pages by hand into a total the rows did not decide")
    void theRegisterPagesByHand() {
        when(subscriptions.findVisible(any(), any(), any(), any(), anyBoolean(), anyInt(), anyLong()))
                .thenReturn(List.of(subscriptionRow()));
        when(subscriptions.countVisible(any(), any(), any(), any(), anyBoolean())).thenReturn(412L);

        Page<PlatformSubscriptionView> page = service.subscriptions(null, PageRequest.of(3, 20));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(412L);
        assertThat(page.getNumber()).isEqualTo(3);
        // The offset is the page's, not the row count's — an off-by-one here reads as duplicated rows.
        verify(subscriptions).findVisible(any(), any(), any(), any(), anyBoolean(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("a plan-less subscription carries no amount rather than a zero one")
    void aPlanlessSubscriptionHasNoAmount() {
        when(subscriptions.findVisible(any(), any(), any(), any(), anyBoolean(), anyInt(), anyLong()))
                .thenReturn(List.of(planlessRow()));
        when(subscriptions.countVisible(any(), any(), any(), any(), anyBoolean())).thenReturn(1L);

        PlatformSubscriptionView row = service.subscriptions(null, PageRequest.of(0, 20)).getContent().getFirst();

        // Zero would be a claim that the store is on a free plan, which is a different fact from having no plan.
        assertThat(row.amount()).isNull();
        assertThat(row.planCode()).isNull();
        assertThat(row.status()).isEqualTo(SubscriptionStatus.PENDING);
    }

    /* ----------------------------------------------------------------------------- the ledger ---- */

    @Test
    @DisplayName("every invoice filter reaches the query, dates included")
    void invoiceFiltersReachTheQuery() {
        givenNoInvoices();
        Instant from = AUGUST;
        Instant to = Instant.parse("2026-09-01T00:00:00Z");

        service.invoices(new ListInvoiceQuery(ORG, STORE, InvoiceStatus.OPEN, from, to), PageRequest.of(0, 20));

        ArgumentCaptor<String> org = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> store = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> status = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Instant> fromAt = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toAt = ArgumentCaptor.forClass(Instant.class);
        verify(invoices).findVisible(org.capture(), store.capture(), status.capture(), fromAt.capture(),
                toAt.capture(), anyInt(), anyLong());

        assertThat(org.getValue()).isEqualTo(ORG.id().toString());
        assertThat(store.getValue()).isEqualTo(STORE.storeMerchantId());
        assertThat(status.getValue()).isEqualTo("OPEN");
        assertThat(fromAt.getValue()).isEqualTo(from);
        assertThat(toAt.getValue()).isEqualTo(to);
    }

    @Test
    @DisplayName("the ledger's money keeps the row's own currency on both figures")
    void theLedgerKeepsItsCurrency() {
        when(invoices.findVisible(any(), any(), any(), any(), any(), anyInt(), anyLong()))
                .thenReturn(List.of(invoiceRow(EUR, 2400L, 2400L)));
        when(invoices.countVisible(any(), any(), any(), any(), any())).thenReturn(1L);

        PlatformInvoiceView row = service.invoices(null, PageRequest.of(0, 20)).getContent().getFirst();

        assertThat(row.amountDue().currency()).isEqualTo(EUR);
        assertThat(row.amountPaid().currency()).isEqualTo(EUR);
        // Minor units end to end: 2400 is 24.00, and nothing here divides by a hundred.
        assertThat(row.amountPaid().minorUnits()).isEqualTo(2400L);
    }

    @Test
    @DisplayName("two currencies come back as two totals rather than one sum")
    void totalsAreNeverMixedAcrossCurrencies() {
        when(invoices.totals(any(), any(), any(), any(), any()))
                .thenReturn(List.of(new InvoiceTotalRow(EUR, 2400L, 3600L, 2L),
                        new InvoiceTotalRow(USD, 5000L, 5000L, 1L)));

        var totals = service.invoiceTotals(new ListInvoiceQuery(ORG, null, null, null, null));

        assertThat(totals).hasSize(2);
        assertThat(totals.getFirst().currency()).isEqualTo(EUR);
        assertThat(totals.getFirst().paid().minorUnits()).isEqualTo(2400L);
        assertThat(totals.getFirst().due().minorUnits()).isEqualTo(3600L);
        assertThat(totals.get(1).currency()).isEqualTo(USD);
        // Nothing on the platform holds an exchange rate, so a mixed total would be a wrong number.
        assertThat(totals).extracting(total -> total.currency().code()).containsExactly(EUR_CODE, USD_CODE);
    }

    @Test
    @DisplayName("the totals call carries the same filter as the rows it sums")
    void totalsUseTheSameFilterAsTheRows() {
        when(invoices.totals(any(), any(), any(), any(), any())).thenReturn(List.of());
        Instant from = AUGUST;

        service.invoiceTotals(new ListInvoiceQuery(ORG, STORE, InvoiceStatus.PAID, from, null));

        // A sum over a wider filter than the rows on screen is worse than no sum: it looks authoritative.
        verify(invoices).totals(ORG.id().toString(), STORE.storeMerchantId(), "PAID", from, null);
    }

    /* ------------------------------------------------------------------------------- fixtures ---- */

    private void givenNoSubscriptions() {
        when(subscriptions.findVisible(any(), any(), any(), any(), anyBoolean(), anyInt(), anyLong()))
                .thenReturn(List.of());
        when(subscriptions.countVisible(any(), any(), any(), any(), anyBoolean())).thenReturn(0L);
    }

    private void givenNoInvoices() {
        when(invoices.findVisible(any(), any(), any(), any(), any(), anyInt(), anyLong())).thenReturn(List.of());
        when(invoices.countVisible(any(), any(), any(), any(), any())).thenReturn(0L);
    }

    private static PlatformSubscriptionRow subscriptionRow() {
        return new PlatformSubscriptionRow(STORE, ORG, SubscriptionStatus.ACTIVE, PRO, "Pro", EUR, 2900L,
                Instant.EPOCH, null, null, null, null, false, true, Instant.EPOCH);
    }

    private static PlatformSubscriptionRow planlessRow() {
        return new PlatformSubscriptionRow(STORE, ORG, SubscriptionStatus.PENDING, null, null, null, null, null,
                null, null, null, null, false, false, Instant.EPOCH);
    }

    private static PlatformInvoiceRow invoiceRow(CurrencyCode currency, Long due, Long paid) {
        return new PlatformInvoiceRow(new com.asrevo.cvhome.billing.commons.StripeInvoiceId("in_1"), STORE, ORG,
                "INV-1", InvoiceStatus.PAID, currency, due, paid, null, null, Instant.EPOCH, Instant.EPOCH, null,
                null);
    }

    @Test
    @DisplayName("an unpaged request still pages rather than reading the whole platform")
    void anUnpagedRequestStillPages() {
        givenNoSubscriptions();

        service.subscriptions(null, Pageable.unpaged());

        ArgumentCaptor<Integer> limit = ArgumentCaptor.forClass(Integer.class);
        verify(subscriptions).findVisible(any(), any(), any(), any(), anyBoolean(), limit.capture(), anyLong());
        assertThat(limit.getValue()).isPositive();
    }

}
