package com.asrevo.cvhome.billing.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.commons.dto.admin.BillingHealthView;
import com.asrevo.cvhome.billing.commons.dto.admin.InvoiceTotal;
import com.asrevo.cvhome.billing.commons.dto.admin.ListAuditQuery;
import com.asrevo.cvhome.billing.commons.dto.admin.ListInvoiceQuery;
import com.asrevo.cvhome.billing.commons.dto.admin.ListSubscriptionQuery;
import com.asrevo.cvhome.billing.commons.dto.admin.PlanStatisticReport;
import com.asrevo.cvhome.billing.commons.dto.admin.PlatformInvoiceView;
import com.asrevo.cvhome.billing.commons.dto.admin.PlatformSubscriptionView;
import com.asrevo.cvhome.billing.commons.dto.admin.SubscriptionAuditView;
import com.asrevo.cvhome.billing.mappers.PlatformBillingMappers;
import com.asrevo.cvhome.billing.repository.ProcessedStripeEventRepository;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.billing.repository.StripeRequestRepository;
import com.asrevo.cvhome.billing.repository.SubscriptionInvoiceRepository;
import com.asrevo.cvhome.billing.service.PlatformBillingService;
import com.asrevo.cvhome.billing.service.SubscriptionAuditService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlatformBillingServiceImpl implements PlatformBillingService {

    /** How many rows a listing falls back to when the caller asks for no page at all. */
    private static final int DEFAULT_PAGE_SIZE = 50;

    /** How far back {@link #health()} looks for failed webhook deliveries. */
    private static final Duration FAILED_EVENT_WINDOW = Duration.ofDays(1);

    /**
     * How long a mutating Stripe call may be in flight before it counts as stalled.
     *
     * <p>
     * Generous on purpose. {@code stripe_request} records the intent <em>before</em> the call, so a row written a
     * second ago is a request in progress rather than a fault; counting those would make the figure permanently
     * non-zero on a busy platform and therefore permanently ignored.
     * </p>
     */
    private static final Duration STALLED_REQUEST_AGE = Duration.ofMinutes(10);

    private final StoreSubscriptionRepository subscriptionRepository;

    private final SubscriptionInvoiceRepository invoiceRepository;

    private final SubscriptionAuditService auditService;

    private final ProcessedStripeEventRepository processedEventRepository;

    private final StripeRequestRepository stripeRequestRepository;

    private final PlatformBillingMappers mappers;

    /**
     * {@inheritDoc}
     *
     * <p>
     * Two queries assembled into a {@link PageImpl} by hand, here and in the two listings below: Spring Data JDBC's
     * {@code @Query} has no {@code countQuery} attribute — that is JPA's — so a paged {@code @Query} cannot be
     * asked for directly. Same shape as tenancy's {@code InternalOrgServiceImpl.findAll}.
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PlatformSubscriptionView> subscriptions(ListSubscriptionQuery query, Pageable pageable) {
        Pageable page = paged(pageable);
        ListSubscriptionQuery filter = query == null
                ? new ListSubscriptionQuery(null, null, null, null, false)
                : query;
        String org = PlatformBillingMappers.idOf(filter.org());
        String status = PlatformBillingMappers.nameOf(filter.status());
        String planCode = PlatformBillingMappers.termOf(filter.planCode());
        String term = PlatformBillingMappers.termOf(filter.term());

        List<PlatformSubscriptionView> rows = subscriptionRepository
                .findVisible(org, status, planCode, term, filter.blockedOnly(), page.getPageSize(), page.getOffset())
                .stream()
                .map(mappers::toView)
                .toList();
        long total = subscriptionRepository.countVisible(org, status, planCode, term, filter.blockedOnly());
        return new PageImpl<>(rows, page, total);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PlatformInvoiceView> invoices(ListInvoiceQuery query, Pageable pageable) {
        Pageable page = paged(pageable);
        ListInvoiceQuery filter = invoiceFilter(query);
        List<PlatformInvoiceView> rows = invoiceRepository
                .findVisible(PlatformBillingMappers.idOf(filter.org()), PlatformBillingMappers.idOf(filter.store()),
                        PlatformBillingMappers.nameOf(filter.status()), filter.from(), filter.to(),
                        page.getPageSize(), page.getOffset())
                .stream()
                .map(mappers::toView)
                .toList();
        long total = invoiceRepository.countVisible(PlatformBillingMappers.idOf(filter.org()),
                PlatformBillingMappers.idOf(filter.store()), PlatformBillingMappers.nameOf(filter.status()),
                filter.from(), filter.to());
        return new PageImpl<>(rows, page, total);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceTotal> invoiceTotals(ListInvoiceQuery query) {
        ListInvoiceQuery filter = invoiceFilter(query);
        return invoiceRepository.totals(PlatformBillingMappers.idOf(filter.org()),
                        PlatformBillingMappers.idOf(filter.store()),
                        PlatformBillingMappers.nameOf(filter.status()), filter.from(), filter.to())
                .stream()
                .map(mappers::toTotal)
                .toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Delegated rather than reimplemented: the trail's own service owns both its writers and its reader, and the
     * propagation the read needs is the one difference between them.
     * </p>
     */
    @Override
    public Page<SubscriptionAuditView> audit(ListAuditQuery query, Pageable pageable) {
        return auditService.search(query, paged(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PlanStatisticReport planStatistics() {
        return new PlanStatisticReport(subscriptionRepository.planSubscriptionCounts(),
                subscriptionRepository.planRecurringValue().stream().map(mappers::toRecurringValue).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BillingHealthView health() {
        Instant now = Instant.now();
        return new BillingHealthView(processedEventRepository.countFailedSince(now.minus(FAILED_EVENT_WINDOW)),
                stripeRequestRepository.countStalledBefore(now.minus(STALLED_REQUEST_AGE)),
                (int) STALLED_REQUEST_AGE.toMinutes());
    }

    /** An absent filter is every invoice, which is what an operator opening the ledger asked for. */
    private ListInvoiceQuery invoiceFilter(ListInvoiceQuery query) {
        return query == null ? new ListInvoiceQuery(null, null, null, null, null) : query;
    }

    private Pageable paged(Pageable pageable) {
        return pageable == null || pageable.isUnpaged() ? Pageable.ofSize(DEFAULT_PAGE_SIZE) : pageable;
    }

}
