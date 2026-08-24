package com.asrevo.cvhome.billing.api.v2;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.billing.commons.dto.admin.BillingHealthView;
import com.asrevo.cvhome.billing.commons.dto.admin.PlanStatisticReport;
import com.asrevo.cvhome.billing.repository.SubscriptionAuditRepository;
import com.asrevo.cvhome.billing.repository.SubscriptionInvoiceRepository;
import com.asrevo.cvhome.billing.service.PlatformBillingService;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StatisticList;
import com.asrevo.cvhome.commons.domain.StatisticRange;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;

/**
 * The platform's billing aggregates.
 *
 * <p>
 * Modelled on tenancy's {@code OrgStatisticApi}: {@code /api/v2}, a {@link StatisticRange} body, a
 * {@link StatisticList} back, and {@code hasRole('ROLE_SUPER_ADMIN')} on every method. These are business metrics
 * for the operator, not tenant data, and none of them is scopeable to one org.
 * </p>
 *
 * <p>
 * <strong>Before this existed, billing summed nothing.</strong> {@code grep} for {@code subscription-statistic}
 * across the repository returned nothing at all, and seller-ui's admin dashboard had been calling that 404 since it
 * was written.
 * </p>
 *
 * <p>
 * The two dated queries below key their day on {@code date()}, which resolves in the <strong>database session's
 * timezone</strong>. The service runs UTC, so a payment at 23:50 local lands on the previous day for an operator
 * elsewhere. Doing it properly means an {@code AT TIME ZONE} parameter on both queries, which is its own change;
 * this is documented rather than half-fixed.
 * </p>
 */
@RestController
@RequestMapping("/api/v2")
@AllArgsConstructor
@Tag(name = "Billing statistic resource", description = "Platform-wide billing aggregates")
public class BillingStatisticApi {

    private final SubscriptionInvoiceRepository invoiceRepository;

    private final SubscriptionAuditRepository auditRepository;

    private final PlatformBillingService platformBillingService;

    /**
     * Money actually collected, per day and per currency.
     *
     * <p>
     * The currency is the entry's {@code name}, so nothing is ever converted or summed across currencies — nothing
     * on this platform holds an exchange rate, and a mixed total is a wrong number rather than a missing one.
     * Amounts are <strong>minor units</strong>, as everything in billing is.
     * </p>
     *
     * <p>
     * Keyed on when the money moved rather than on when the invoice was raised; see
     * {@code SubscriptionInvoiceRepository.revenueStatistic} for why that choice is not cosmetic.
     * </p>
     */
    @PostMapping("/private/revenue-statistic")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public StatisticList revenueStatistic(@RequestBody StatisticRange range) {
        List<StatisticEntry> entries = invoiceRepository.revenueStatistic(range.fromDate().toInstant(),
                range.toDate().toInstant());
        return new StatisticList(entries);
    }

    /**
     * Subscriptions started per day, by plan code.
     *
     * <p>
     * "Started" means started paying or trialling, which is why it reads the audit trail rather than
     * {@code store_subscription.created_date} — that column counts every store that ever entered billing, which is
     * already what {@code store-statistic} answers.
     * </p>
     *
     * <p>
     * Grouped by plan code so the console can stack the series. The two tenancy counters' {@code name} is null;
     * these are the first entries on the platform where it is not.
     * </p>
     */
    @PostMapping("/private/subscription-statistic")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public StatisticList subscriptionStatistic(@RequestBody StatisticRange range) {
        List<StatisticEntry> entries = auditRepository.subscriptionStatistic(range.fromDate().toInstant(),
                range.toDate().toInstant());
        return new StatisticList(entries);
    }

    /**
     * The commercial reading of the plan catalogue: who is on what, and what that is contracted to bring in.
     *
     * <p>
     * Its own record rather than a {@link StatisticList}, because it carries counts <em>and</em> money in two
     * dimensions and {@code StatisticEntry.value} is a single {@code Number}. No range: it is a reading of the
     * present, not of a period.
     * </p>
     */
    @GetMapping("/private/plan-statistic")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public PlanStatisticReport planStatistic() {
        return platformBillingService.planStatistics();
    }

    /**
     * Whether billing itself is working, from two tables nothing has ever read.
     *
     * <p>
     * Cheap enough that leaving it out and writing the gap down instead would have been the more expensive choice:
     * one count each, and together they are the only "billing is broken right now" signal the platform has.
     * </p>
     */
    @GetMapping("/private/billing-health")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public BillingHealthView billingHealth() {
        return platformBillingService.health();
    }

}
