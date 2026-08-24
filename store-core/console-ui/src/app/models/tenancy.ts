/**
 * Tenancy's store shapes. Corrections against the seller-core original, all verified against
 * `tenancy-commons/dto/ManagerStoreDto.java` and the enums beside it:
 *
 * - `status` and `billingStatus` were missing entirely. The server sends both.
 * - `provisioningState` was typed `string`; it is a four-value enum, and the console has to branch on it.
 * - `provisioningError` is new on the server: a FAILED row now carries the pod's own refusal, which the console
 *   shows beside the failure instead of leaving "failed" as the whole story.
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
  /**
   * Why provisioning failed, in the pod's words — a diagnostic string, not a translated message.
   *
   * Null for every state but `FAILED_PROVISIONING`, and null there too for a store that failed before tenancy
   * started recording it. Shown verbatim beside the translated failure line rather than in place of it.
   */
  readonly provisioningError: string | null;
}

/**
 * An email address as `commons/domain/Email` sends it — `{"email": "…"}`, not a bare string.
 *
 * A single-component Java record with no `@JsonValue`, so Jackson writes it as an object. seller-core
 * typed it the same way; the shape is worth naming rather than inlining, because three DTOs carry it.
 */
export interface Email {
  readonly email: string;
}

/**
 * Whether an organization may be used. Mirrors tenancy `commons/dto/OrgStatus`.
 *
 * Only `ACTIVE` is operable, and the check is read at the *org* rather than fanned out to its stores:
 * `InternalStoreService.requireOperable` reads both, so suspending an org takes every store it owns
 * offline without writing to any of them.
 */
export type OrgStatus = 'ACTIVE' | 'SUSPENDED' | 'CLOSED';

/**
 * An organization, as tenancy describes it. Mirrors `manager/commons/dto/ManagerOrgDto` (record).
 *
 * **Wider than seller-core's `Org`,** which was `{id, email, createdDate}` and predates the lifecycle
 * work. `name`, `status` and `ownerUserId` have been on the wire since; carrying them is what makes a
 * status column and the lifecycle actions possible at all.
 *
 * `id` is a `ManagerOrgId`, so it arrives wrapped — `{"id": "65f0…"}` — while `ownerUserId` is a bare
 * uaa UUID string.
 *
 * - `name` is null for every organization created before it existed, and for every one created since:
 *   `ManagerOrgEntity.createOrgFromUser` sets no name and `rename` is the only writer. See
 *   lessons.md, "Organizations — an org cannot be named at creation".
 * - `ownerUserId` is null for every row on the platform until the backfill in this module runs, and
 *   for none created after it — see lessons.md, "Organizations — the owner nobody recorded".
 */
export interface ManagerOrgDto {
  readonly id: IdentityId;
  readonly email: Email | null;
  /** `Instant`, so an ISO-8601 timestamp. */
  readonly createdDate: string;
  readonly name: string | null;
  readonly status: OrgStatus | null;
  readonly ownerUserId: string | null;
}

/**
 * How many stores sit on one pod. Mirrors tenancy `commons/dto/PodStoreCount` (record).
 *
 * Tenancy owns `manager_store.pod_id`, so this is the authoritative count. Pod-registry keeps its own
 * `pod.capacity_stores`, but that is a mirror maintained from tenancy's outbox and knows only about stores placed
 * through it — the two can disagree, which is why this exists.
 *
 * **A pod with no stores does not appear at all** rather than appearing as zero: the query groups and there is
 * nothing to group. Callers read a missing pod as none placed, never as unknown.
 */
export interface PodStoreCount {
  readonly podId: IdentityId;
  readonly stores: number;
}

/** A pod reference as the create form sends it — `{"pod": {"id": "…"}}`. */
export interface PodRef {
  readonly id: string;
}

/** The store's registered address, in merchant's `PersistableBaseAddress` shape. */
export interface CreateStoreAddress {
  /** ISO 3166-1 alpha-2. The only part tenancy requires — `merchant_store.country_id` is NOT NULL. */
  readonly country: string;
  readonly stateProvince?: string;
  /** The street line. Named `address` because merchant's `BaseAddress` names it that. */
  readonly address?: string;
  readonly city?: string;
  readonly postalCode?: string;
}

/**
 * What tenancy needs to create a store.
 *
 * Tenancy types `name` and `pod` as its own; the rest belongs to merchant's store model and is forwarded to the
 * pod. It is **not** an open bag, though, and used to be typed as one here. Everything below is either read by
 * tenancy or validated by it — `CreateStoreRequest` on the server declares `@NotBlank` on the identity and
 * storefront fields and `@NotEmpty` on `supportedLanguages`, because the pod refuses a create without them and
 * provisioning is asynchronous: a body this endpoint accepts and the pod later rejects surfaces as a failed store
 * minutes later rather than as a 400 this form can bind.
 *
 * The optional fields are the ones the pod merely tolerates. They are forwarded untouched.
 */
export interface CreateStoreRequest {
  readonly name: string;
  /** Omitted entirely when the merchant has no pod to choose; the registry then places the store. */
  readonly pod?: PodRef;
  readonly email: string;
  readonly phone: string;
  /** A `Theme` constant, from `store-manager/public/themes`. */
  readonly theme: string;
  /** A `ColorTheme` constant, from `store-manager/public/color-themes`. */
  readonly colorTheme: string;
  /** ISO 4217. */
  readonly currency: string;
  /** Must be one of `supportedLanguages` — the server does not check that, but a storefront cannot render otherwise. */
  readonly defaultLanguage: string;
  readonly supportedLanguages: readonly string[];
  readonly address: CreateStoreAddress;
  /** `LocalDate`: `YYYY-MM-DD`. Omitted rather than sent empty — `''` does not parse. */
  readonly inBusinessSince?: string;
  /** `MeasureUnit`. */
  readonly dimension?: string;
  /** `WeightUnit`. */
  readonly weight?: string;
  readonly requireLoginForOrderPlacement?: boolean;
}

/** What `GET store-manager/private/store/unique?name=` answers. */
export interface EntityExists {
  readonly exists: boolean;
}
