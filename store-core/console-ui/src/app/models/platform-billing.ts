/**
 * Console-native. Billing as the *platform* reads it.
 *
 * Kept apart from `@models/billing`, which is one store's own subscription and the public catalogue,
 * because the two have different audiences and different guards: everything here is
 * `hasRole('ROLE_SUPER_ADMIN')` and none of it is store-scoped. The types they genuinely share —
 * `Money`, `SubscriptionStatus`, `InvoiceStatus`, `Identifier` — are imported rather than restated,
 * so a currency written one way on the merchant billing page cannot be written another way here.
 *
 * **Identifiers do not all arrive the same way, and that is the server's shape rather than a
 * choice made here.** `StoreMerchantId` carries `@JsonValue` and serialises as a bare string;
 * `ManagerOrgId`, `PlanId` and `StripeInvoiceId` are records whose component is `id`, so they arrive
 * wrapped as `{"id": "…"}`. Both forms are typed honestly below and flattened in the api service —
 * the seam that exists precisely so a page never has to know which is which.
 */
import type {Identifier, InvoiceStatus, Money, SubscriptionStatus} from '@models/billing';
import type {Tone} from '@models/ui';

/* ------------------------------------------------------------------- the wire ---- */

/** Mirrors `billing.commons.dto.admin.PlatformSubscriptionView`. */
export interface PlatformSubscriptionDto {
  /** Bare string: `StoreMerchantId` is `@JsonValue`. */
  readonly store: string;
  readonly org: Identifier | null;
  readonly status: SubscriptionStatus;
  readonly planCode: string | null;
  readonly planDisplayName: string | null;
  /** Null where the store has no plan — which is not the same as a plan that costs nothing. */
  readonly amount: Money | null;
  readonly currentPeriodEnd: string | null;
  readonly trialEnd: string | null;
  readonly graceUntil: string | null;
  readonly suspendedAt: string | null;
  readonly canceledAt: string | null;
  readonly cancelAtPeriodEnd: boolean;
  /** Whether a provider subscription stands behind the row. Gates every lever; status does not. */
  readonly providerLinked: boolean;
  readonly createdDate: string | null;
}

/** Mirrors `PlatformInvoiceView`. */
export interface PlatformInvoiceDto {
  readonly id: Identifier;
  readonly store: string;
  readonly org: Identifier | null;
  readonly number: string | null;
  readonly status: InvoiceStatus;
  readonly amountDue: Money | null;
  readonly amountPaid: Money | null;
  readonly periodStart: string | null;
  readonly periodEnd: string | null;
  readonly issuedAt: string | null;
  readonly paidAt: string | null;
  readonly hostedInvoiceUrl: string | null;
  readonly invoicePdfUrl: string | null;
}

/** Mirrors `SubscriptionAuditView`. */
export interface SubscriptionAuditDto {
  readonly id: number;
  readonly store: string;
  readonly org: Identifier | null;
  readonly eventType: AuditEventType;
  readonly fromStatus: SubscriptionStatus | null;
  readonly toStatus: SubscriptionStatus | null;
  readonly fromPlanCode: string | null;
  readonly toPlanCode: string | null;
  readonly source: AuditSource;
  /** Null on every `API` row written before the actor was threaded through. Rendered as unknown. */
  readonly actor: string | null;
  readonly stripeEventId: Identifier | null;
  readonly detail: string | null;
  readonly occurredAt: string;
}

/** Mirrors `InvoiceTotal`. One entry per currency; never a sum across them. */
export interface InvoiceTotalDto {
  readonly currency: {readonly code: string};
  readonly paid: Money | null;
  readonly due: Money | null;
  readonly invoices: number;
}

/** Mirrors `BillingHealthView`. */
export interface BillingHealthDto {
  readonly failedEvents: number;
  readonly stalledRequests: number;
  readonly staleAfterMinutes: number;
}

/** Mirrors `PlanSubscriptionCount`. `planCode` is null for the stores that have no plan at all. */
export interface PlanSubscriptionCountDto {
  readonly planCode: string | null;
  readonly planDisplayName: string | null;
  readonly tier: number | null;
  readonly status: SubscriptionStatus;
  readonly subscriptions: number;
}

/** Mirrors `PlanRecurringValue`. Both scales ship, so nothing here ever divides. */
export interface PlanRecurringValueDto {
  readonly planCode: string | null;
  readonly status: SubscriptionStatus;
  readonly subscriptions: number;
  readonly monthly: Money | null;
  readonly annual: Money | null;
}

/** Mirrors `PlanStatisticReport`. */
export interface PlanStatisticDto {
  readonly counts: readonly PlanSubscriptionCountDto[];
  readonly recurringValue: readonly PlanRecurringValueDto[];
}

/* --------------------------------------------------------------- the vocabulary ---- */

/**
 * `billing.commons.AuditEventType`, all sixteen.
 *
 * Every one has a translation, because a row of any of them could appear tomorrow. Only
 * {@link FILTERABLE_AUDIT_EVENTS} is offered in the dropdown, which is a shorter list — see there.
 */
export type AuditEventType =
  | 'CREATED'
  | 'TRIAL_STARTED'
  | 'ACTIVATED'
  | 'RENEWED'
  | 'PAYMENT_FAILED'
  | 'PAST_DUE'
  | 'SUSPENDED'
  | 'RESUMED'
  | 'PLAN_UPGRADED'
  | 'PLAN_DOWNGRADE_SCHEDULED'
  | 'PLAN_DOWNGRADE_APPLIED'
  | 'CANCEL_SCHEDULED'
  | 'CANCEL_REVOKED'
  | 'CANCELED'
  | 'INVOICE_RECORDED'
  | 'QUOTA_REFUSED';

/** Guards the Transloco lookup: Transloco throws on a missing key, so an unknown value is humanized. */
export const AUDIT_EVENT_TYPES: ReadonlySet<string> = new Set<string>([
  'CREATED',
  'TRIAL_STARTED',
  'ACTIVATED',
  'RENEWED',
  'PAYMENT_FAILED',
  'PAST_DUE',
  'SUSPENDED',
  'RESUMED',
  'PLAN_UPGRADED',
  'PLAN_DOWNGRADE_SCHEDULED',
  'PLAN_DOWNGRADE_APPLIED',
  'CANCEL_SCHEDULED',
  'CANCEL_REVOKED',
  'CANCELED',
  'INVOICE_RECORDED',
  'QUOTA_REFUSED',
]);

/**
 * The thirteen the filter offers — the sixteen above, less three.
 *
 * `RESUMED`, `INVOICE_RECORDED` and `QUOTA_REFUSED` are in the enum and in the table's `CHECK`
 * constraint and **are written by nothing**: grep the writers in `billing-service` and no call site
 * passes any of them. `RESUMED` in particular looks like the obvious choice for un-cancelling and is
 * not — that path writes `CANCEL_REVOKED`. Offering three options that always return nothing reads
 * as a broken filter, so the dropdown is narrowed while the translations stay complete, because a
 * row of any of them could be written tomorrow and must render.
 */
export const FILTERABLE_AUDIT_EVENTS: readonly AuditEventType[] = [
  'CREATED',
  'TRIAL_STARTED',
  'ACTIVATED',
  'RENEWED',
  'PAYMENT_FAILED',
  'PAST_DUE',
  'SUSPENDED',
  'PLAN_UPGRADED',
  'PLAN_DOWNGRADE_SCHEDULED',
  'PLAN_DOWNGRADE_APPLIED',
  'CANCEL_SCHEDULED',
  'CANCEL_REVOKED',
  'CANCELED',
];

/** `billing.commons.ChangeSource`: who drove a change. */
export type AuditSource = 'API' | 'WEBHOOK' | 'JOB' | 'SYSTEM';

export const AUDIT_SOURCES: readonly AuditSource[] = ['API', 'WEBHOOK', 'JOB', 'SYSTEM'];

export const AUDIT_SOURCE_SET: ReadonlySet<string> = new Set<string>(AUDIT_SOURCES);

export const INVOICE_STATUSES: readonly string[] = ['DRAFT', 'OPEN', 'PAID', 'UNCOLLECTIBLE', 'VOID'];

export const INVOICE_STATUS_SET: ReadonlySet<string> = new Set<string>(INVOICE_STATUSES);

/**
 * Invoice status to colour.
 *
 * `UNCOLLECTIBLE` is the only red: it is money the platform gave up on. `VOID` is slate because a
 * cancelled invoice owes nothing and is not a fault, and `DRAFT` is slate for the same reason —
 * Stripe has not finalised it, so its figures may still change.
 */
export const INVOICE_STATUS_TONE: Readonly<Record<string, Tone>> = {
  DRAFT: 'slate',
  OPEN: 'amber',
  PAID: 'green',
  UNCOLLECTIBLE: 'red',
  VOID: 'slate',
};

/**
 * Audit event to colour.
 *
 * Grouped by what the event means for the money rather than by verb: anything that starts or renews
 * a paying relationship is green, anything scheduled is blue because it has not happened yet, and
 * the three that end or interrupt one are amber or red.
 */
export const AUDIT_EVENT_TONE: Readonly<Record<AuditEventType, Tone>> = {
  CREATED: 'slate',
  TRIAL_STARTED: 'cyan',
  ACTIVATED: 'green',
  RENEWED: 'green',
  PAYMENT_FAILED: 'amber',
  PAST_DUE: 'amber',
  SUSPENDED: 'red',
  RESUMED: 'green',
  PLAN_UPGRADED: 'green',
  PLAN_DOWNGRADE_SCHEDULED: 'blue',
  PLAN_DOWNGRADE_APPLIED: 'amber',
  CANCEL_SCHEDULED: 'blue',
  CANCEL_REVOKED: 'green',
  CANCELED: 'red',
  INVOICE_RECORDED: 'slate',
  QUOTA_REFUSED: 'amber',
};

/** Where a change came from, as a colour. `API` is the only one a person did. */
export const AUDIT_SOURCE_TONE: Readonly<Record<AuditSource, Tone>> = {
  API: 'violet',
  WEBHOOK: 'blue',
  JOB: 'slate',
  SYSTEM: 'slate',
};

/**
 * Subscription status to colour, for the platform's own tables.
 *
 * The same map the merchant billing page uses, which is why it is here rather than in either
 * feature: the platform register and one store's own page must not disagree about what `PAST_DUE`
 * looks like.
 */
export const SUBSCRIPTION_STATUS_TONE: Readonly<Record<SubscriptionStatus, Tone>> = {
  ACTIVE: 'green',
  TRIALING: 'cyan',
  PENDING: 'amber',
  PAST_DUE: 'amber',
  SUSPENDED: 'red',
  CANCELED: 'red',
};

/**
 * The three states billing refuses to serve — `EntitlementServiceImpl.BLOCKED`.
 *
 * Here as well as on the server because the register's *count* of blocked rows is drawn from a page
 * the console already has, while the filter is the server's. Both have to mean the same three.
 */
export const BLOCKED_STATUSES: ReadonlySet<SubscriptionStatus> = new Set<SubscriptionStatus>([
  'PENDING',
  'SUSPENDED',
  'CANCELED',
]);

/* ------------------------------------------------------------------- view models ---- */

/** One subscription, as the register's table renders it. */
export interface PlatformSubscriptionRow {
  readonly store: string;
  readonly orgId: string;
  readonly status: SubscriptionStatus;
  readonly planCode: string | null;
  readonly planName: string | null;
  readonly amount: Money | null;
  readonly currentPeriodEnd: string | null;
  readonly trialEnd: string | null;
  readonly graceUntil: string | null;
  readonly providerLinked: boolean;
  readonly cancelAtPeriodEnd: boolean;
  /**
   * When billing stopped serving this store, or null when it has not.
   *
   * `suspendedAt` or `canceledAt`, whichever the row has — the two are different endings and the
   * status column already says which, so the date column only has to say when.
   */
  readonly blockedSince: string | null;
  readonly blocked: boolean;
}

/** One invoice, as the ledger's table renders it. */
export interface PlatformInvoiceRow {
  readonly id: string;
  readonly store: string;
  readonly orgId: string;
  readonly number: string;
  readonly status: InvoiceStatus;
  readonly amountDue: Money | null;
  readonly amountPaid: Money | null;
  readonly issuedAt: string | null;
  readonly paidAt: string | null;
  readonly hostedInvoiceUrl: string | null;
  readonly invoicePdfUrl: string | null;
}

/** One line of the audit trail, as the Activity table renders it. */
export interface AuditRow {
  readonly id: number;
  readonly store: string;
  readonly orgId: string;
  readonly eventType: AuditEventType;
  readonly fromStatus: SubscriptionStatus | null;
  readonly toStatus: SubscriptionStatus | null;
  readonly fromPlanCode: string | null;
  readonly toPlanCode: string | null;
  readonly source: AuditSource;
  /** Null means the table did not record who — not that nobody did. The column says so. */
  readonly actor: string | null;
  readonly occurredAt: string;
}

/* ---------------------------------------------------------------------- mappers ---- */

/** The string inside a wrapped identifier, or `''`. */
function idOf(value: Identifier | null | undefined): string {
  return value?.id ?? '';
}

export function toSubscriptionRow(dto: PlatformSubscriptionDto): PlatformSubscriptionRow {
  return {
    store: dto.store ?? '',
    orgId: idOf(dto.org),
    status: dto.status,
    planCode: dto.planCode,
    planName: dto.planDisplayName ?? dto.planCode,
    amount: dto.amount,
    currentPeriodEnd: dto.currentPeriodEnd,
    trialEnd: dto.trialEnd,
    graceUntil: dto.graceUntil,
    providerLinked: dto.providerLinked,
    cancelAtPeriodEnd: dto.cancelAtPeriodEnd,
    blockedSince: dto.suspendedAt ?? dto.canceledAt,
    blocked: BLOCKED_STATUSES.has(dto.status),
  };
}

export function toInvoiceRow(dto: PlatformInvoiceDto): PlatformInvoiceRow {
  return {
    id: idOf(dto.id),
    store: dto.store ?? '',
    orgId: idOf(dto.org),
    // Stripe numbers an invoice only when it finalises one, so a DRAFT row genuinely has none. The
    // provider id is the next best handle, and it is what a support conversation quotes anyway.
    number: dto.number ?? idOf(dto.id),
    status: dto.status,
    amountDue: dto.amountDue,
    amountPaid: dto.amountPaid,
    issuedAt: dto.issuedAt,
    paidAt: dto.paidAt,
    hostedInvoiceUrl: dto.hostedInvoiceUrl,
    invoicePdfUrl: dto.invoicePdfUrl,
  };
}

export function toAuditRow(dto: SubscriptionAuditDto): AuditRow {
  return {
    id: dto.id,
    store: dto.store ?? '',
    orgId: idOf(dto.org),
    eventType: dto.eventType,
    fromStatus: dto.fromStatus,
    toStatus: dto.toStatus,
    fromPlanCode: dto.fromPlanCode,
    toPlanCode: dto.toPlanCode,
    source: dto.source,
    actor: dto.actor,
    occurredAt: dto.occurredAt,
  };
}
