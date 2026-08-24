/**
 * Pods are the physical per-region deployments a store is placed into.
 *
 * **Two records, deliberately.** `Pod` is the minimal *routing* contract — it is on every service's
 * classpath, the gateway rebuilds its route table from it every minute, and `StorePodClientFactory`
 * resolves against it. `PodView` is everything the registry itself knows, and is reachable only by id.
 * The paged list answers `Pod`, so lifecycle, health and capacity cannot be columns — see lessons.md,
 * "Pods — the paged list returns the routing record, not the view".
 *
 * A correction to what this file used to say: it claimed `PodEntity` carries no region or residency.
 * It carries `region`, `capacity_max_stores`, `capacity_stores`, `last_health_status` and
 * `last_health_at`, and `PodView` returns all five. What is missing is a *merchant-readable* endpoint
 * — see lessons.md, "Shell — no merchant-readable list of placeable pods".
 */
import type {IdentityId} from '@models/tenancy';

export type EndpointType = 'EXTERNAL' | 'INTERNAL';

export interface Endpoint {
  readonly endpoint: string;
  readonly type: EndpointType;
}

/**
 * The routing record: what `list`, the paged list, `create` and `update` all speak.
 *
 * `domain` is on the wire and is **always null** — `PodEntity.toPod()` passes a literal null and
 * `newEntity` never reads it. Declared so the shape is not a surprise; never sent.
 */
export interface Pod {
  readonly id?: IdentityId;
  readonly name: string;
  /** Read-only on the server: `@JsonProperty(access = READ_ONLY)`, derived from the first 8 chars of the id. */
  readonly shortenPodId?: string;
  readonly endpoint: Endpoint;
  /** Null for a public pod. seller-core's type said a pod always has an owner; it does not. */
  readonly orgId: IdentityId | null;
  readonly domain?: string | null;
}

/**
 * Who may be placed on a pod. Mirrors `pod-registry-commons/PodVisibility`.
 *
 * Its own column rather than `orgId != null`, so an operator can hold a pod out of public rotation
 * without inventing an owner for it. Settable only at creation, and then only indirectly:
 * `PodEntity.newEntity` derives it from whether an org was named — see lessons.md, "Pods —
 * visibility, region, capacity and owner cannot be edited".
 */
export type PodVisibility = 'PUBLIC' | 'PRIVATE';

/**
 * Where a pod is in its operational life. Mirrors `pod-registry-commons/PodLifecycleState`.
 *
 * **Lifecycle gates placement, never routing.** A `DRAINING` or `DECOMMISSIONED` pod keeps its
 * gateway route, because the stores already on it are live and withdrawing the route would break
 * working storefronts to fix nothing.
 *
 * Only two of the four transitions have an endpoint — `drain` and `resume`. A newly registered pod is
 * written straight to `ACTIVE` by `newEntity`, and nothing can retire one. See lessons.md, "Pods —
 * two lifecycle states are unreachable".
 */
export type PodLifecycleState = 'PROVISIONING' | 'ACTIVE' | 'DRAINING' | 'DECOMMISSIONED';

/**
 * The last health probe's verdict. Mirrors `pod-registry-commons/PodHealthStatus`.
 *
 * Health gates placement only, for the same reason lifecycle does: a RED pod's tenants already live
 * there, and withdrawing the route turns "degraded" into "entirely offline".
 */
export type PodHealthStatus = 'GREEN' | 'AMBER' | 'RED';

/**
 * A pod as the registry describes it. Mirrors `pod-registry-commons/dto/PodView` (record).
 *
 * `openToPlacement()` and `hasRoom()` exist on the Java record and are **not on the wire** — Jackson
 * serializes a record's components, and neither method carries the `@JsonProperty` that `Pod`'s
 * `shortenPodId()` needed to appear. Both are derived here instead.
 *
 * `capacityMaxStores` null means unlimited, which is what every pod on the platform is today.
 */
export interface PodView {
  readonly id: IdentityId;
  readonly name: string;
  readonly endpoint: Endpoint;
  readonly orgId: IdentityId | null;
  readonly visibility: PodVisibility | null;
  readonly lifecycleState: PodLifecycleState | null;
  readonly region: string | null;
  readonly capacityMaxStores: number | null;
  readonly capacityStores: number;
  readonly lastHealthStatus: PodHealthStatus | null;
  /** `Instant`, so an ISO-8601 timestamp. Null until a probe has ever run — which is every pod today. */
  readonly lastHealthAt: string | null;
}

/** Whether a new store may be placed here, ignoring ownership and capacity. `PodView.openToPlacement`. */
export function openToPlacement(pod: PodView): boolean {
  return pod.lifecycleState === 'ACTIVE';
}

/** Whether the pod has room, treating a null ceiling as unlimited. `PodView.hasRoom`. */
export function hasRoom(pod: PodView): boolean {
  return pod.capacityMaxStores === null || pod.capacityStores < pod.capacityMaxStores;
}
