/**
 * Console-native; no seller-core original worth the name.
 *
 * The platform console's view models — organizations, pods and platform accounts as the four
 * `/platform/*` screens render them. seller-ui had two of these screens and typed their rows inline
 * from three-field interfaces; everything below is read off a DTO that was already on the wire.
 *
 * The tone maps and the known-value sets live here rather than beside the components that use them
 * because two features read each: the organizations list and the organization detail agree on what
 * colour `SUSPENDED` is, and the pod list and the pod detail agree on what `DRAINING` means.
 */
import type {PodHealthStatus, PodLifecycleState, PodVisibility, PodView} from '@models/pod';
import type {
  ManagerOrgDto,
  ManagerStore,
  OrgStatus,
  ProvisioningState,
  StoreStatus,
  SubscriptionStatus,
} from '@models/tenancy';
import type {Tone} from '@cvhome-saas/ui-kit';

/* ------------------------------------------------------------------ organizations ---- */

/**
 * One organization, as the tenant registry's table renders it.
 *
 * `name` is empty for every organization created so far — nothing sets one at creation — so the
 * table falls back to the contact email rather than leaving the identity column blank. The fallback
 * is computed once, here, so the list and the detail header agree on what an unnamed org is called.
 */
export interface OrgRow {
  readonly id: string;
  /** The recorded name, or `''`. Never the fallback — see {@link label}. */
  readonly name: string;
  /** What to show as the organization's identity: its name, or its contact email. */
  readonly label: string;
  readonly email: string;
  readonly status: OrgStatus | null;
  /** ISO-8601, as `Instant` sends it. */
  readonly createdDate: string;
  /**
   * uaa's id for the owner, or null.
   *
   * Null means the owner password reset cannot be offered — see lessons.md, "Organizations — the
   * owner nobody recorded". The backfill resolves the historical rows; one it could not resolve
   * stays null and the console says why rather than offering a button that 422s.
   */
  readonly ownerUserId: string | null;
}

/**
 * One organization, as a table row.
 *
 * Here rather than in a feature's api service because two features shape the same DTO — the list
 * and the detail header — and a second copy of the fallback rule is how the two would end up
 * disagreeing about what an unnamed organization is called.
 */
export function toOrgRow(org: ManagerOrgDto): OrgRow {
  const name = org.name?.trim() ?? '';
  const email = org.email?.email ?? '';
  return {
    id: org.id?.id ?? '',
    name,
    label: name || email,
    email,
    status: org.status,
    createdDate: org.createdDate,
    ownerUserId: org.ownerUserId,
  };
}

/** Every organization status the console has words for. Guards the Transloco lookup. */
export const ORG_STATUSES: ReadonlySet<string> = new Set<string>(['ACTIVE', 'SUSPENDED', 'CLOSED']);

/**
 * Status to colour.
 *
 * `CLOSED` is slate rather than red: it is an ending, not a fault, and the reds on this page are
 * reserved for the two operations that break something.
 */
export const ORG_STATUS_TONE: Readonly<Record<OrgStatus, Tone>> = {
  ACTIVE: 'green',
  SUSPENDED: 'amber',
  CLOSED: 'slate',
};

/** The lifecycle moves the server exposes, in the order the header offers them. */
export type OrgLifecycleAction = 'suspend' | 'resume' | 'close';

/* --------------------------------------------------------------------------- pods ---- */

/**
 * One pod, as the fleet table renders it.
 *
 * Everything here comes off the routing `Pod` the paged list answers with. Lifecycle, health and
 * capacity are deliberately absent: they live on `PodView`, which is per-id only, and a column that
 * costs one request per row is not a column. See lessons.md, "Pods — the paged list returns the
 * routing record, not the view".
 */
export interface PodRow {
  readonly id: string;
  readonly name: string;
  /** The first eight characters of the id, as the server derives them. */
  readonly shortId: string;
  readonly endpoint: string;
  readonly endpointType: string;
  /** The owning organization's id, or null for a shared pod. */
  readonly orgId: string | null;
  /**
   * How many stores tenancy has placed here, or null when the count could not be read.
   *
   * Zero and null are different answers and are rendered differently: zero is an empty pod, null is a tenancy
   * that did not respond. The count rides along as an optional leg, so losing it costs a column rather than the
   * table.
   */
  readonly stores: number | null;
}

export const POD_LIFECYCLE_STATES: ReadonlySet<string> = new Set<string>([
  'PROVISIONING',
  'ACTIVE',
  'DRAINING',
  'DECOMMISSIONED',
]);

export const POD_HEALTH_STATUSES: ReadonlySet<string> = new Set<string>(['GREEN', 'AMBER', 'RED']);

export const POD_VISIBILITIES: ReadonlySet<string> = new Set<string>(['PUBLIC', 'PRIVATE']);

/**
 * Lifecycle to colour.
 *
 * `DRAINING` is amber and not red, because a draining pod is working perfectly — it is simply not
 * taking new tenants. Reading it as a fault is what would make an operator do something rash.
 */
export const POD_LIFECYCLE_TONE: Readonly<Record<PodLifecycleState, Tone>> = {
  PROVISIONING: 'blue',
  ACTIVE: 'green',
  DRAINING: 'amber',
  DECOMMISSIONED: 'slate',
};

/** The probe's own three-colour vocabulary, mapped onto the console's. */
export const POD_HEALTH_TONE: Readonly<Record<PodHealthStatus, Tone>> = {
  GREEN: 'green',
  AMBER: 'amber',
  RED: 'red',
};

export const POD_VISIBILITY_TONE: Readonly<Record<PodVisibility, Tone>> = {
  PUBLIC: 'cyan',
  PRIVATE: 'violet',
};

/** One pod's full state, as the detail page reads it. Straight from `PodView`, with the id unwrapped. */
export interface PodDetail {
  readonly id: string;
  readonly name: string;
  readonly shortId: string;
  readonly endpoint: string;
  readonly endpointType: string;
  readonly orgId: string | null;
  readonly visibility: PodVisibility | null;
  readonly lifecycleState: PodLifecycleState | null;
  readonly region: string | null;
  /** Null means unlimited, which is what every pod on the platform is today. */
  readonly capacityMaxStores: number | null;
  readonly capacityStores: number;
  readonly lastHealthStatus: PodHealthStatus | null;
  readonly lastHealthAt: string | null;
  /** `PodView.openToPlacement()`, derived here because the method is not on the wire. */
  readonly openToPlacement: boolean;
  /** `PodView.hasRoom()`, likewise. */
  readonly hasRoom: boolean;
}

/* ------------------------------------------------------------------ platform users ---- */

/*
 * `PlatformUserRow` and `toPlatformUserRow` moved to `@cvhome-saas/ui-kit/uaa`, with the table that
 * renders them and the client that fetches them. Re-exported here because six files in the platform
 * console already name them from this module.
 */
export {toPlatformUserRow, type PlatformUserRow} from '@cvhome-saas/ui-kit/uaa';

/* ------------------------------------------------------- stores, on the platform side ---- */

/**
 * One store as the platform console reads it: read-only, because nothing on this side may edit one.
 *
 * Named for the *console half* rather than for one screen, because two render it — an organization's Stores tab
 * and a pod's. Each draws the columns it has something to say about: the org tab already knows the organization,
 * the pod page already knows the pod.
 */
export interface PlatformStoreRow {
  readonly id: string;
  readonly name: string;
  /** The owning organization. Drawn by the pod's list, which spans tenants; the org tab already knows it. */
  readonly orgId: string;
  readonly status: StoreStatus | null;
  readonly provisioningState: ProvisioningState | null;
  readonly podId: string;
  readonly provisioningError: string | null;
  /**
   * Whether the store is paid for — the one billing fact that rides along on a tenancy store row.
   *
   * It arrives free: tenancy's `InternalStoreServiceImpl.withBillingStatus` batch-fills it from
   * billing on every store page it serves, as a service principal. It was once the *only* way a
   * platform operator learned anything about a store's billing, because billing's own endpoints
   * checked `hasReadAccessOnStore` and that had no super-admin branch; `/platform/billing` reads the
   * rest now. See lessons.md, "Platform — a store's subscription cannot be read by an operator
   * *(answered)*".
   *
   * **Null is not a lapse**, and it has two causes the console cannot tell apart. Billing's batch
   * snapshot returns a row only for a store it *has* a subscription for — `findAllByStoreIds`, no
   * placeholder — so a store created before billing existed, or one whose `StoreCreatedEvent` never
   * landed, is simply absent. And tenancy's read fails open on any billing failure, deliberately, so
   * an outage looks the same. Both are "not known", which is what the cell says.
   */
  readonly billingStatus: SubscriptionStatus | null;
}

/** One store, from tenancy's own row. */
export function toPlatformStoreRow(store: ManagerStore): PlatformStoreRow {
  return {
    id: store.id,
    name: store.name,
    orgId: store.orgId?.id ?? '',
    status: store.status,
    provisioningState: store.provisioningState,
    podId: store.podId?.id ?? '',
    provisioningError: store.provisioningError,
    billingStatus: store.billingStatus,
  };
}

/** Every store status the console has words for. */
export const STORE_STATUSES: ReadonlySet<string> = new Set<string>([
  'ACTIVE',
  'SUSPENDED',
  'ARCHIVED',
  'DELETED',
]);

export const STORE_STATUS_TONE: Readonly<Record<StoreStatus, Tone>> = {
  ACTIVE: 'green',
  SUSPENDED: 'amber',
  ARCHIVED: 'slate',
  DELETED: 'slate',
};

export const PROVISIONING_STATES: ReadonlySet<string> = new Set<string>([
  'NOT_STARTED_PROVISIONING',
  'IN_PROGRESS_PROVISIONING',
  'SUCCESSFULLY_PROVISIONING',
  'FAILED_PROVISIONING',
]);

/** Every subscription status the console has words for. Mirrors billing's `SubscriptionStatus`. */
export const SUBSCRIPTION_STATUSES: ReadonlySet<string> = new Set<string>([
  'PENDING',
  'TRIALING',
  'ACTIVE',
  'PAST_DUE',
  'SUSPENDED',
  'CANCELED',
]);

/**
 * Subscription status to colour.
 *
 * `PENDING` is slate rather than amber: billing learns about a store from an event, so a store
 * created seconds ago is legitimately pending and is nobody's problem. The amber pair are the two
 * that need someone to act, and `CANCELED` is the only ending.
 */
export const SUBSCRIPTION_STATUS_TONE: Readonly<Record<SubscriptionStatus, Tone>> = {
  PENDING: 'slate',
  TRIALING: 'blue',
  ACTIVE: 'green',
  PAST_DUE: 'amber',
  SUSPENDED: 'amber',
  CANCELED: 'red',
};

export const PROVISIONING_STATE_TONE: Readonly<Record<ProvisioningState, Tone>> = {
  NOT_STARTED_PROVISIONING: 'slate',
  IN_PROGRESS_PROVISIONING: 'blue',
  SUCCESSFULLY_PROVISIONING: 'green',
  FAILED_PROVISIONING: 'red',
};

/* ------------------------------------------------------------------------- charts ---- */

/** One day of a platform counter, as the dashboard's trend panels plot it. */
export interface TrendPoint {
  /** ISO day, `2026-08-04`. */
  readonly date: string;
  readonly value: number;
}

/* --------------------------------------------------------------- pod shaping ---- */

/**
 * `PodView` as the detail page reads it: ids unwrapped, and the two derived predicates the Java
 * record exposes as methods and Jackson therefore does not send.
 */
export function toPodDetail(pod: PodView): PodDetail {
  const id = pod.id?.id ?? '';
  return {
    id,
    name: pod.name,
    shortId: id.slice(0, 8),
    endpoint: pod.endpoint?.endpoint ?? '',
    endpointType: pod.endpoint?.type ?? '',
    orgId: pod.orgId?.id ?? null,
    visibility: pod.visibility,
    lifecycleState: pod.lifecycleState,
    region: pod.region,
    capacityMaxStores: pod.capacityMaxStores,
    capacityStores: pod.capacityStores,
    lastHealthStatus: pod.lastHealthStatus,
    lastHealthAt: pod.lastHealthAt,
    openToPlacement: pod.lifecycleState === 'ACTIVE',
    hasRoom: pod.capacityMaxStores === null || pod.capacityStores < pod.capacityMaxStores,
  };
}
