package com.asrevo.cvhome.billing.api.v1;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.billing.commons.dto.admin.InvoiceTotal;
import com.asrevo.cvhome.billing.commons.dto.admin.ListAuditQuery;
import com.asrevo.cvhome.billing.commons.dto.admin.ListInvoiceQuery;
import com.asrevo.cvhome.billing.commons.dto.admin.ListSubscriptionQuery;
import com.asrevo.cvhome.billing.commons.dto.admin.PlatformInvoiceView;
import com.asrevo.cvhome.billing.commons.dto.admin.PlatformSubscriptionView;
import com.asrevo.cvhome.billing.commons.dto.admin.SubscriptionAuditView;
import com.asrevo.cvhome.billing.service.PlatformBillingService;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

/**
 * Billing as the platform sees it: every subscription, every invoice, and the whole audit trail.
 *
 * <p>
 * <strong>Super admin only, on every method.</strong> Not {@code hasPermission(#store, …)} like the store-scoped
 * controllers beside it — there is no store to check, because these are the platform-wide questions. The store-scoped
 * ones stay as they are and narrow their queries by the caller's org.
 * </p>
 *
 * <p>
 * A {@code POST} carrying a query body wherever there is more than one optional filter, matching tenancy's
 * {@code store-manager/list} and {@code org-manager/list}. Paging is {@code page} and <strong>{@code count}</strong>
 * as query parameters — {@code store-commons:autoconfigure}'s {@code ServletWebConfig} renames Spring's {@code size}
 * platform-wide, so a {@code size} here would be silently ignored and every page would come back at the default.
 * </p>
 *
 * <p>
 * <strong>This is what replaces {@code entitlement/private/blocked-stores} for a human.</strong> That endpoint
 * answers a bare list of store ids — no reason, no since-when — sits on the gateway's once-a-minute hot path, and is
 * gated on a token that <em>also</em> opens {@code ExternalStoreQuotaApi.provision}, which creates a subscription
 * and spends the org's one trial through a primary key that can never be reclaimed. Widening that token so a report
 * could be drawn would be a one-way door reached through what reads like a read. {@link #subscriptions} with
 * {@code blockedOnly} answers the same question strictly better: the same three statuses, plus the reason, the date,
 * the org and the plan on every row.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/platform")
@RequiredArgsConstructor
@Tag(name = "Platform billing resource", description = "Billing across every tenant")
public class PlatformBillingApi {

    private final PlatformBillingService platformBillingService;

    /** Every subscription on the platform, narrowed by the query. */
    @PostMapping("subscriptions")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public Page<PlatformSubscriptionView> subscriptions(@RequestBody ListSubscriptionQuery query, Pageable pageable) {
        return platformBillingService.subscriptions(query, pageable);
    }

    /** The ledger: every invoice on the platform, newest first. */
    @PostMapping("invoices")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public Page<PlatformInvoiceView> invoices(@RequestBody ListInvoiceQuery query, Pageable pageable) {
        return platformBillingService.invoices(query, pageable);
    }

    /**
     * What the same filter comes to, one figure per currency.
     *
     * <p>
     * A second call on the same body rather than a field on the page above, so the rows can render while the sums
     * are still being computed.
     * </p>
     */
    @PostMapping("invoices/totals")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public List<InvoiceTotal> invoiceTotals(@RequestBody ListInvoiceQuery query) {
        return platformBillingService.invoiceTotals(query);
    }

    /**
     * The subscription audit trail — the one screen that answers "who moved this store onto the cheaper plan, and
     * when".
     *
     * <p>
     * Ordered {@code occurred_at desc, id desc}: two rows written in one transaction share their timestamp, and the
     * sequence is what stops a row appearing on two pages or on neither.
     * </p>
     */
    @PostMapping("audit")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public Page<SubscriptionAuditView> audit(@RequestBody ListAuditQuery query, Pageable pageable) {
        return platformBillingService.audit(query, pageable);
    }

}
