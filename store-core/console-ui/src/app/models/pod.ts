/**
 * Ported from seller-ui/projects/seller-core/stores/src/lib/services/pod.service.ts (the inline types).
 *
 * Pods are the physical per-region deployments a store is placed into.
 *
 * Note what is **not** here: region, latency and data residency. `PodEntity` carries none of them, so the
 * create-store design's region cards cannot be built from this — see lessons.md, "Shell — no
 * merchant-readable list of placeable pods".
 */
import type {IdentityId} from '@models/tenancy';

export type EndpointType = 'EXTERNAL' | 'INTERNAL';

export interface Endpoint {
  readonly endpoint: string;
  readonly type: EndpointType;
}

export interface Pod {
  readonly id?: IdentityId;
  readonly name: string;
  readonly shortenPodId: string;
  readonly endpoint: Endpoint;
  readonly orgId: IdentityId | null;
}
