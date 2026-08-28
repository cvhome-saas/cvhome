package com.asrevo.cvhome.billing.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.billing.commons.dto.admin.BillingHealthView;
import com.asrevo.cvhome.billing.commons.dto.admin.InvoiceTotal;
import com.asrevo.cvhome.billing.commons.dto.admin.ListAuditQuery;
import com.asrevo.cvhome.billing.commons.dto.admin.ListInvoiceQuery;
import com.asrevo.cvhome.billing.commons.dto.admin.ListSubscriptionQuery;
import com.asrevo.cvhome.billing.commons.dto.admin.PlanStatisticReport;
import com.asrevo.cvhome.billing.commons.dto.admin.PlatformInvoiceView;
import com.asrevo.cvhome.billing.commons.dto.admin.PlatformSubscriptionView;
import com.asrevo.cvhome.billing.commons.dto.admin.SubscriptionAuditView;

/**
 * Billing across every tenant — the reads a platform operator needs and no merchant may have.
 *
 * <p>
 * Nothing here takes a store, and nothing here is tenant-scoped: these <em>are</em> the platform-wide questions, and
 * every one of them is gated on {@code ROLE_SUPER_ADMIN} at the controller rather than on a per-store permission
 * token. That is the distinction the store-scoped {@link SubscriptionService} and {@link InvoiceService} beside it
 * keep: those two narrow their queries by the caller's org and refuse to answer outside it.
 * </p>
 */
public interface PlatformBillingService {

    /** Every subscription on the platform, narrowed by the query. */
    Page<PlatformSubscriptionView> subscriptions(ListSubscriptionQuery query, Pageable pageable);

    /** Every invoice on the platform, narrowed by the query, newest first. */
    Page<PlatformInvoiceView> invoices(ListInvoiceQuery query, Pageable pageable);

    /**
     * What the same filter comes to, one figure per currency.
     *
     * <p>
     * A second call on the same body rather than a field on the page: the ledger's rows can render while the sums
     * are still being computed, and a response that is sometimes a page and sometimes a page-plus-envelope is the
     * shape the console's {@code SpringPage} type exists to avoid.
     * </p>
     */
    List<InvoiceTotal> invoiceTotals(ListInvoiceQuery query);

    /** The subscription audit trail, newest first. */
    Page<SubscriptionAuditView> audit(ListAuditQuery query, Pageable pageable);

    /** Who is on what, and what that is worth — the commercial reading of the catalogue. */
    PlanStatisticReport planStatistics();

    /** Whether billing itself is working. */
    BillingHealthView health();

}
