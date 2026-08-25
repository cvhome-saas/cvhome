package com.asrevo.cvhome.billing.service.impl;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.billing.commons.AuditEventType;
import com.asrevo.cvhome.billing.commons.ChangeSource;
import com.asrevo.cvhome.billing.commons.InvoiceStatus;
import com.asrevo.cvhome.billing.commons.Money;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.StripeEventId;
import com.asrevo.cvhome.billing.commons.StripeInvoiceId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.InvoiceView;
import com.asrevo.cvhome.billing.commons.dto.admin.ListAuditQuery;
import com.asrevo.cvhome.billing.commons.dto.admin.SubscriptionAuditView;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.domain.SubscriptionAuditEntity;
import com.asrevo.cvhome.billing.domain.SubscriptionInvoiceEntity;
import com.asrevo.cvhome.billing.mappers.InvoiceMappers;
import com.asrevo.cvhome.billing.repository.SubscriptionAuditRepository;
import com.asrevo.cvhome.billing.repository.SubscriptionInvoiceRepository;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A store's own invoice history, and the audit trail's writers and reader.
 *
 * <p>
 * The invoice listing carries the same tenant guard as {@code SubscriptionApi}: the shared permission checker cannot
 * tell which org a store belongs to, so the boundary lives in which query is chosen. The audit reader assembles its
 * page by hand because Spring Data JDBC's {@code @Query} has no {@code countQuery}, which means the total is a
 * second query and nothing but a test stops it drifting from the rows.
 * </p>
 */
class InvoiceAndAuditServiceTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final ManagerOrgId ORG = new ManagerOrgId("32a034a43cd77581d105c87a");

    private static final Instant ISSUED = Instant.parse("2026-01-01T00:00:00Z");

    private SubscriptionInvoiceRepository invoices;

    private SubscriptionAuditRepository audits;

    private InvoiceServiceImpl invoiceService;

    private SubscriptionAuditServiceImpl auditService;

    @BeforeEach
    void setUp() {
        invoices = mock(SubscriptionInvoiceRepository.class);
        audits = mock(SubscriptionAuditRepository.class);
        invoiceService = new InvoiceServiceImpl(invoices, new InvoiceMappers());
        auditService = new SubscriptionAuditServiceImpl(audits);
    }

    private static SubscriptionInvoiceEntity invoice() {
        return SubscriptionInvoiceEntity.record(new StripeInvoiceId("in_1"), STORE, ORG,
                        new StripeSubscriptionId("sub_1"), "CVH-0001", InvoiceStatus.PAID,
                        new Money(new CurrencyCode("USD"), 3000L), 3000L, ISSUED)
                .covering(ISSUED, ISSUED.plusSeconds(2_592_000L))
                .hostedAt("https://invoice.stripe.test/in_1", "https://invoice.stripe.test/in_1.pdf");
    }

    // ---------------------------------------------------------------------------------------------- invoices

    @Test
    @DisplayName("an org-scoped listing goes through the query that names the org")
    void invoiceListingIsScoped() {
        when(invoices.findAllByStoreIdAndOrgIdOrderByIssuedAtDesc(eq(STORE), eq(ORG), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(invoice())));

        Page<InvoiceView> page = invoiceService.list(STORE, ORG, PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1);
        // Enforced in the query rather than trusted to the permission layer: without it one org's admin could read
        // another org's spend.
        verify(invoices, never()).findAllByStoreIdOrderByIssuedAtDesc(any(), any());
    }

    @Test
    @DisplayName("a null scope reads every invoice of the store, for a caller that spans orgs")
    void invoiceListingUnscoped() {
        when(invoices.findAllByStoreIdOrderByIssuedAtDesc(eq(STORE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(invoice())));

        invoiceService.list(STORE, null, PageRequest.of(0, 20));

        verify(invoices).findAllByStoreIdOrderByIssuedAtDesc(eq(STORE), any(Pageable.class));
    }

    @Test
    @DisplayName("an invoice renders the amounts, the window and both links")
    void invoiceRendering() {
        when(invoices.findAllByStoreIdOrderByIssuedAtDesc(eq(STORE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(invoice())));

        InvoiceView view = invoiceService.list(STORE, null, PageRequest.of(0, 20)).getContent().getFirst();

        assertThat(view.id()).isEqualTo(new StripeInvoiceId("in_1"));
        assertThat(view.number()).isEqualTo("CVH-0001");
        assertThat(view.status()).isEqualTo(InvoiceStatus.PAID);
        // Minor units end to end, which is what Stripe speaks — no rounding step between the catalog and an invoice.
        assertThat(view.amountDue().minorUnits()).isEqualTo(3000L);
        assertThat(view.amountPaid().minorUnits()).isEqualTo(3000L);
        assertThat(view.issuedAt()).isEqualTo(ISSUED);
        assertThat(view.hostedInvoiceUrl()).isEqualTo("https://invoice.stripe.test/in_1");
        assertThat(view.invoicePdfUrl()).isEqualTo("https://invoice.stripe.test/in_1.pdf");
    }

    // ------------------------------------------------------------------------------------------------- audit

    @Test
    @DisplayName("an API-driven change is recorded with who asked and where it came from")
    void recordsAnApiChange() throws Exception {
        StoreSubscriptionEntity after = StoreSubscriptionEntity.pending(STORE, ORG)
                .activate(PlanId.newId(), null, ISSUED, ISSUED);
        PlanId fromPlan = PlanId.newId();

        auditService.record(SubscriptionStatus.PENDING, fromPlan, after, AuditEventType.ACTIVATED,
                ChangeSource.API, "owner@example.test");

        ArgumentCaptor<SubscriptionAuditEntity> saved = ArgumentCaptor.forClass(SubscriptionAuditEntity.class);
        verify(audits).save(saved.capture());
        assertThat(saved.getValue().getFromStatus()).isEqualTo(SubscriptionStatus.PENDING);
        assertThat(saved.getValue().getToStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(saved.getValue().getFromPlanId()).isEqualTo(fromPlan);
        assertThat(saved.getValue().getSource()).isEqualTo(ChangeSource.API);
        assertThat(saved.getValue().getActor()).isEqualTo("owner@example.test");
        assertThat(saved.getValue().getStripeEventId()).isNull();
    }

    @Test
    @DisplayName("a webhook-driven change names the provider, not the customer, and the event that caused it")
    void recordsAWebhookChange() throws Exception {
        StoreSubscriptionEntity after = StoreSubscriptionEntity.pending(STORE, ORG)
                .activate(PlanId.newId(), null, ISSUED, ISSUED);

        auditService.recordFromWebhook(SubscriptionStatus.PENDING, null, after, AuditEventType.ACTIVATED,
                new StripeEventId("evt_1"));

        ArgumentCaptor<SubscriptionAuditEntity> saved = ArgumentCaptor.forClass(SubscriptionAuditEntity.class);
        verify(audits).save(saved.capture());
        assertThat(saved.getValue().getSource()).isEqualTo(ChangeSource.WEBHOOK);
        // Not the customer: the customer's act was the payment, and this is the provider telling us what came of it.
        assertThat(saved.getValue().getActor()).isEqualTo("stripe");
        // The link back to the delivery, which is what makes a disputed charge traceable.
        assertThat(saved.getValue().getStripeEventId()).isEqualTo(new StripeEventId("evt_1"));
    }

    @Test
    @DisplayName("the trail's total comes from its own count query, not from the rows on the page")
    void theTotalIsASecondQuery() {
        when(audits.findVisible(any(), any(), any(), any(), any(), any(), anyInt(), anyLong()))
                .thenReturn(List.of(mock(SubscriptionAuditView.class)));
        when(audits.countVisible(any(), any(), any(), any(), any(), any())).thenReturn(137L);

        Page<SubscriptionAuditView> page = auditService.search(null, PageRequest.of(0, 20));

        // A page whose total came from the row count would report the platform as twenty audit rows wide forever.
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(137L);
    }

    @Test
    @DisplayName("an absent filter narrows on nothing rather than on nulls that mean something")
    void anAbsentFilterNarrowsOnNothing() {
        when(audits.findVisible(any(), any(), any(), any(), any(), any(), anyInt(), anyLong()))
                .thenReturn(List.of());

        auditService.search(null, Pageable.unpaged());

        // Every filter is `cast(:x as varchar) is null or col = :x`, so "not applied" and "applied as null" are the
        // same SQL and cannot be told apart by reading the result — only by checking what was passed.
        verify(audits).findVisible(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(50), eq(0L));
    }

    @Test
    @DisplayName("the filters that were given reach the query as their own string forms")
    void filtersReachTheQuery() {
        when(audits.findVisible(any(), any(), any(), any(), any(), any(), anyInt(), anyLong()))
                .thenReturn(List.of());
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-02-01T00:00:00Z");

        auditService.search(new ListAuditQuery(STORE, ORG, AuditEventType.CANCELED, ChangeSource.API, from, to),
                PageRequest.of(2, 10));

        verify(audits).findVisible(eq(STORE.getId().toString()), eq(ORG.getId().toString()), eq("CANCELED"),
                eq("API"), eq(from), eq(to), eq(10), eq(20L));
    }

}
