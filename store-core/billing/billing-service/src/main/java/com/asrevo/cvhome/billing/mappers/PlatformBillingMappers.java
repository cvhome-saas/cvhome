package com.asrevo.cvhome.billing.mappers;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.billing.commons.Money;
import com.asrevo.cvhome.billing.commons.dto.admin.InvoiceTotal;
import com.asrevo.cvhome.billing.commons.dto.admin.PlanRecurringValue;
import com.asrevo.cvhome.billing.commons.dto.admin.PlatformInvoiceView;
import com.asrevo.cvhome.billing.commons.dto.admin.PlatformSubscriptionView;
import com.asrevo.cvhome.billing.repository.projection.InvoiceTotalRow;
import com.asrevo.cvhome.billing.repository.projection.PlanRecurringValueRow;
import com.asrevo.cvhome.billing.repository.projection.PlatformInvoiceRow;
import com.asrevo.cvhome.billing.repository.projection.PlatformSubscriptionRow;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.Identifier;

/**
 * Turns the platform's flat query rows into the DTOs that ship.
 *
 * <p>
 * Nothing here reads the catalogue, and that is the point. {@code SubscriptionMappers} resolves the plan, the price
 * and the entitlement map through {@code PlanCatalogService} on every row — an uncached primary-key read apiece —
 * which is right for one store's own page and ruinous for a paged register. The queries behind these rows have
 * already joined what the screen needs.
 * </p>
 */
@Component
public class PlatformBillingMappers {

    /** How many months are in the year the run rate is expressed at. Named so the two uses cannot drift. */
    private static final long MONTHS_IN_YEAR = 12L;

    /**
     * The string form of an identifier, or {@code null}.
     *
     * <p>
     * Null-safe twice over: the filter may be absent, and a {@link com.asrevo.cvhome.commons.domain.ManagerOrgId}
     * built from something that is not 24 characters carries a <em>null</em> inner id rather than failing to
     * construct. A malformed id in a query body should narrow to nothing or not narrow at all — it should not be an
     * NPE inside a read.
     * </p>
     */
    public static String idOf(Identifier identifier) {
        if (identifier == null || identifier.getId() == null) {
            return null;
        }
        return identifier.getId().toString();
    }

    /** The name of an enum filter, or {@code null} for "do not narrow on this". */
    public static String nameOf(Enum<?> value) {
        return value == null ? null : value.name();
    }

    /** A search term, trimmed, or {@code null} for a blank one — a cleared box and an untouched one are one case. */
    public static String termOf(String term) {
        if (term == null || term.trim().isEmpty()) {
            return null;
        }
        return term.trim();
    }

    public PlatformSubscriptionView toView(PlatformSubscriptionRow row) {
        return new PlatformSubscriptionView(row.store(), row.org(), row.status(), row.planCode(),
                row.planDisplayName(), money(row.currency(), row.unitAmount()), row.currentPeriodEnd(),
                row.trialEnd(), row.graceUntil(), row.suspendedAt(), row.canceledAt(), row.cancelAtPeriodEnd(),
                row.providerLinked(), row.createdDate());
    }

    public PlatformInvoiceView toView(PlatformInvoiceRow row) {
        return new PlatformInvoiceView(row.id(), row.store(), row.org(), row.number(), row.status(),
                money(row.currency(), row.amountDue()), money(row.currency(), row.amountPaid()), row.periodStart(),
                row.periodEnd(), row.issuedAt(), row.paidAt(), row.hostedInvoiceUrl(), row.invoicePdfUrl());
    }

    public InvoiceTotal toTotal(InvoiceTotalRow row) {
        return new InvoiceTotal(row.currency(), money(row.currency(), row.paid()), money(row.currency(), row.due()),
                row.invoices());
    }

    /**
     * The run rate of one plan in one currency, at both scales.
     *
     * <p>
     * The annual figure is the database's sum and the monthly one is derived from it by a single division. The other
     * way round — dividing each yearly price by twelve and summing — truncates once per row.
     * </p>
     */
    public PlanRecurringValue toRecurringValue(PlanRecurringValueRow row) {
        long annual = row.annual() == null ? 0L : row.annual();
        return new PlanRecurringValue(row.planCode(), row.status(), row.subscriptions(),
                money(row.currency(), annual / MONTHS_IN_YEAR), money(row.currency(), annual));
    }

    /**
     * An amount, or {@code null} where there is no currency to express it in.
     *
     * <p>
     * A plan-less subscription has no price row, so both columns come back null. Zero would be a claim that the
     * store is on a free plan, which is a different fact.
     * </p>
     */
    private Money money(CurrencyCode currency, Long minorUnits) {
        if (currency == null || minorUnits == null) {
            return null;
        }
        return new Money(currency, minorUnits);
    }

}
