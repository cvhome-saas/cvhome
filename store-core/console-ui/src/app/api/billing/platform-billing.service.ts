/** Console-native: billing has never had a platform-wide read before, so there is no seller-core original. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@cvhome-saas/ui-kit';
import type {SpringPage} from '@cvhome-saas/ui-kit';
import type {
  AuditEventType,
  AuditSource,
  BillingHealthDto,
  InvoiceTotalDto,
  PlanStatisticDto,
  PlatformInvoiceDto,
  PlatformSubscriptionDto,
  SubscriptionAuditDto,
} from '@models/platform-billing';
import type {InvoiceStatus, SubscriptionStatus} from '@models/billing';

/** `PlatformBillingApi`, behind the gateway's `/billing` prefix. */
const PLATFORM_BILLING_BASE = '/billing/api/v1/platform';

/** `BillingStatisticApi`. `/api/v2` for the aggregates, matching tenancy's counters. */
const BILLING_STATISTIC_BASE = '/billing/api/v2/private';

/** What narrows the subscription register. Every field is optional. */
export interface SubscriptionQuery {
  /** An organization id, or `''` for every org. */
  readonly org?: string;
  readonly status?: SubscriptionStatus | '';
  readonly planCode?: string;
  /** A substring of the store id, matched server-side and case-insensitively. */
  readonly term?: string;
  /** Only the stores billing refuses to serve — `PENDING`, `SUSPENDED` or `CANCELED`. */
  readonly blockedOnly?: boolean;
}

/** What narrows the invoice ledger. The dates are ISO-8601 instants over `issued_at`. */
export interface InvoiceQuery {
  readonly org?: string;
  readonly store?: string;
  readonly status?: InvoiceStatus | '';
  readonly from?: string | null;
  readonly to?: string | null;
}

/** What narrows the audit trail. The dates are ISO-8601 instants over `occurred_at`. */
export interface AuditQuery {
  readonly store?: string;
  readonly org?: string;
  readonly eventType?: AuditEventType | '';
  readonly source?: AuditSource | '';
  readonly from?: string | null;
  readonly to?: string | null;
}

/**
 * Billing as the platform sees it.
 *
 * Kept apart from `subscription.service.ts` — the store-scoped half — because the two have different
 * audiences and different guards. Everything here is `hasRole('ROLE_SUPER_ADMIN')` and takes no
 * store; everything there is `hasPermission(#store, …)` and narrows to the caller's org.
 *
 * **Paging is `count`, never Spring's `size`.** `store-commons:autoconfigure`'s `ServletWebConfig`
 * renames the page-size parameter platform-wide, so a `size` here is silently ignored and every page
 * comes back at the server's default. That is the mistake `org.service.spec.ts` was written to
 * catch, and the spec beside this file catches it again.
 *
 * **Ids are wrapped where the server wraps them.** `ManagerOrgId` is a record whose component is
 * `id`, so a body filter is `{org: {id: '…'}}`; `StoreMerchantId` carries `@JsonValue` and is a bare
 * string. Sending the wrong one binds to null and silently widens the filter to the whole platform,
 * which is the worst possible failure for a filtered read — so both forms are asserted in the spec.
 *
 * **What is deliberately not here:** any money operation. There is no comp, no credit note and no
 * trial extension, because each needs a real Stripe call and an audit event type that does not
 * exist. See lessons.md, "Platform billing — no comp, credit note or trial extension".
 */
@Injectable({providedIn: 'root'})
export class PlatformBillingService {
  private readonly crudService = inject(CrudService);

  /** Every subscription on the platform, narrowed by the query. */
  subscriptions(
    query: SubscriptionQuery,
    page: number,
    count: number,
  ): Observable<SpringPage<PlatformSubscriptionDto>> {
    return this.crudService.post(
      `${PLATFORM_BILLING_BASE}/subscriptions`,
      {
        org: wrapId(query.org),
        status: query.status || null,
        planCode: query.planCode || null,
        term: query.term?.trim() || null,
        // A boolean rather than a nullable list: "blocked" is three statuses, and the query's
        // `cast(:x as varchar) is null` idiom cannot express a nullable list.
        blockedOnly: query.blockedOnly === true,
      },
      {page, count},
    );
  }

  /** The ledger, newest first. */
  invoices(query: InvoiceQuery, page: number, count: number): Observable<SpringPage<PlatformInvoiceDto>> {
    return this.crudService.post(`${PLATFORM_BILLING_BASE}/invoices`, invoiceBody(query), {page, count});
  }

  /**
   * What the same filter comes to, one figure per currency.
   *
   * A second call on the same body rather than a field on the page, so the rows render while the
   * sums are still being computed. **Never summed across currencies** — nothing on the platform
   * holds an exchange rate, so a mixed total would be a wrong number rather than a missing one.
   */
  invoiceTotals(query: InvoiceQuery): Observable<InvoiceTotalDto[]> {
    return this.crudService.post(`${PLATFORM_BILLING_BASE}/invoices/totals`, invoiceBody(query));
  }

  /**
   * The subscription audit trail, newest first.
   *
   * Every plan change and payment failure since billing was written is in this table, and until now
   * nothing read it: `SubscriptionAuditService` was a write-only interface.
   */
  audit(query: AuditQuery, page: number, count: number): Observable<SpringPage<SubscriptionAuditDto>> {
    return this.crudService.post(
      `${PLATFORM_BILLING_BASE}/audit`,
      {
        // A bare string: `StoreMerchantId` serialises with `@JsonValue`, unlike the wrapped org id.
        store: query.store || null,
        org: wrapId(query.org),
        eventType: query.eventType || null,
        source: query.source || null,
        from: query.from ?? null,
        to: query.to ?? null,
      },
      {page, count},
    );
  }

  /** Who is on what, right now, and what that is contracted to bring in. A GET: it takes no range. */
  planStatistics(): Observable<PlanStatisticDto> {
    return this.crudService.get(`${BILLING_STATISTIC_BASE}/plan-statistic`);
  }

  /** Two counts from tables nothing else reads: failed webhooks, and Stripe calls that never returned. */
  health(): Observable<BillingHealthDto> {
    return this.crudService.get(`${BILLING_STATISTIC_BASE}/billing-health`);
  }
}

/**
 * An identifier as the server's record shape wants it, or null.
 *
 * `ManagerOrgId` is `record ManagerOrgId(ObjectId id)`, so Jackson reads `{"id": "…"}`. A bare string
 * would bind the whole record to null and quietly widen the filter to every organization.
 */
function wrapId(id: string | undefined): {id: string} | null {
  return id ? {id} : null;
}

/**
 * The invoice filter as one body, shared by the rows and their totals.
 *
 * One function rather than two literals, because a sum computed over a wider filter than the rows on
 * screen is worse than no sum at all: it looks authoritative.
 */
function invoiceBody(query: InvoiceQuery): Record<string, unknown> {
  return {
    org: wrapId(query.org),
    store: query.store || null,
    status: query.status || null,
    from: query.from ?? null,
    to: query.to ?? null,
  };
}
