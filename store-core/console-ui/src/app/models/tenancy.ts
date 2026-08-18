/**
 * Ported from seller-ui/projects/seller-core/src/lib/models/commons.ts, corrected against the server.
 *
 * Tenancy's store shapes. Corrections against the seller-core original, all verified against
 * `tenancy-commons/dto/ManagerStoreDto.java` and the enums beside it:
 *
 * - `status` and `billingStatus` were missing entirely. The server sends both.
 * - `provisioningState` was typed `string`; it is a four-value enum, and the console has to branch on it.
 */

export interface IdentityId {
  readonly id: string;
}

export type PodId = IdentityId;

/** How far the store got in being built. Unrelated to whether it is paid for. */
export type ProvisioningState =
  | 'NOT_STARTED_PROVISIONING'
  | 'IN_PROGRESS_PROVISIONING'
  | 'SUCCESSFULLY_PROVISIONING'
  | 'FAILED_PROVISIONING';

/**
 * Whether the store may be used at all — an operator's lever, unlike `ProvisioningState`.
 * Only `ACTIVE` is operable; the console never offers the others as transitions.
 */
export type StoreStatus = 'ACTIVE' | 'SUSPENDED' | 'ARCHIVED' | 'DELETED';

/** Mirrors billing's `SubscriptionStatus`. */
export type SubscriptionStatus =
  | 'PENDING'
  | 'TRIALING'
  | 'ACTIVE'
  | 'PAST_DUE'
  | 'SUSPENDED'
  | 'CANCELED';

/**
 * Mirrors tenancy `manager/commons/dto/ManagerStoreDto` (record).
 *
 * `id` is a bare string: a store id serializes as `"65f0…"`, unlike `orgId` and `podId`, which are
 * still `{id: "…"}` objects.
 *
 * `billingStatus` is read from billing rather than stored by tenancy, and is **null when billing could
 * not be reached**. Callers must render that as "unknown" rather than as a problem — a billing outage
 * is not a reason to tell a merchant their store has lapsed. The Java doc comment says exactly this.
 */
export interface ManagerStore {
  readonly id: string;
  readonly name: string;
  readonly orgId: IdentityId;
  readonly podId: PodId;
  readonly provisioningState: ProvisioningState;
  readonly status: StoreStatus;
  readonly billingStatus: SubscriptionStatus | null;
}

/** A pod reference as the create form sends it — `{"pod": {"id": "…"}}`. */
export interface PodRef {
  readonly id: string;
}

/**
 * What tenancy needs to create a store.
 *
 * Only `name` and `pod` are typed on the server; everything else is collected by `@JsonAnySetter`
 * into an `additional` map and forwarded to the pod untouched, because it belongs to merchant's
 * store model rather than tenancy's. So this stays open — the extra keys are real and are the
 * merchant store's own fields.
 */
export interface CreateStoreRequest {
  readonly name: string;
  /** Omitted entirely when the merchant has no pod to choose; the registry then places the store. */
  readonly pod?: PodRef;
  readonly [field: string]: unknown;
}

/** What `GET store-manager/private/store/unique?name=` answers. */
export interface EntityExists {
  readonly exists: boolean;
}
