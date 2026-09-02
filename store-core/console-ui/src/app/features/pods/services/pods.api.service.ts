import {Injectable, inject} from '@angular/core';
import {Observable, forkJoin, map} from 'rxjs';

import {PodService} from '@api/pod-registry/pod.service';
import {ManagerStoreService} from '@api/tenancy/manager-store.service';
import {OrgService} from '@api/tenancy/org.service';
import {optionalOne} from '@cvhome-saas/ui-kit';
import type {PageRequest} from '@cvhome-saas/ui-kit';
import {toOrgRow, type PodRow} from '@models/platform';
import type {Pod} from '@models/pod';

/**
 * How many organizations are read to turn a pod's `orgId` into a name.
 *
 * There is no bulk org lookup and no `findAllByIds`, so the only way to name an owner is to hold a
 * page of the registry in hand. A pod owned by an organization beyond this shows its id instead —
 * which is what seller-ui showed for every pod. See lessons.md, "Organizations — the list cannot be
 * searched, filtered or sorted": a `findByIds` would make this exact.
 */
export const ORG_LOOKUP_LIMIT = 200;

/** What the fleet asks for: a page and a search term. */
export interface PodsQuery extends PageRequest {
  /** Matched server-side against the pod's name and endpoint. */
  readonly term: string;
}

/** Everything the fleet table renders for one query. */
export interface PodsSnapshot {
  readonly rows: readonly PodRow[];
  readonly totalElements: number;
  readonly totalPages: number;
  /** Organization id to display name, for the owner column. Incomplete by construction. */
  readonly orgNames: ReadonlyMap<string, string>;
  /**
   * The term these rows answer, echoed back.
   *
   * The table keeps the last good rows up while the next request is in flight, so the empty state
   * has to know whether "nothing matched" describes the current term or the previous one's results.
   */
  readonly term: string;
}

/**
 * The pod fleet.
 *
 * **The paged list answers the routing record, not the registry's view.** Lifecycle, visibility,
 * region, capacity and health are on `PodView`, which is per-id only, so none of them can be a
 * column here — a column costing one request per row is not a column. They appear on the detail
 * page. See lessons.md, "Pods — the paged list returns the routing record, not the view".
 *
 * The organization names ride along as an **optional** leg: an owner shown as an id is still a
 * usable table, and losing the fleet to the tenant registry would be a bad trade. The store counts
 * are optional for the same reason.
 *
 * The search is the registry's own — `?q=` matches the name and the endpoint — so it narrows the
 * whole fleet rather than the page on screen.
 */
@Injectable({providedIn: 'root'})
export class PodsApi {
  private readonly pods = inject(PodService);
  private readonly orgs = inject(OrgService);
  private readonly stores = inject(ManagerStoreService);

  loadPods(query: PodsQuery): Observable<PodsSnapshot> {
    return forkJoin({
      page: this.pods.page(query.page, query.count, query.term),
      // Optional: this only turns an owner's id into a name. `optionalOne`, not `optionalList` —
      // the endpoint answers a page envelope, and "unknown" is the honest absence here.
      orgs: this.orgs.list(0, ORG_LOOKUP_LIMIT).pipe(optionalOne()),
      // Optional for the same reason: a missing count costs the column, not the fleet.
      counts: this.stores.storesPerPod().pipe(optionalOne()),
    }).pipe(
      map(({page, orgs, counts}) => {
        /*
         * `null` means the count could not be read at all; a pod *absent* from a successful answer means no
         * stores, because the query groups and there is nothing to group. The two must not collapse into one,
         * so the map is built only when there was an answer and the row reads a miss as zero in that case.
         */
        const byPod = counts === null ? null : new Map(counts.map((it) => [it.podId?.id ?? '', it.stores] as const));
        return {
          rows: (page.content ?? []).map((pod) => toPodRow(pod, byPod)),
          totalElements: page.totalElements,
          totalPages: page.totalPages,
          orgNames: new Map((orgs?.content ?? []).map(toOrgRow).map((org) => [org.id, org.label] as const)),
          term: query.term,
        };
      }),
    );
  }
}

/**
 * One pod, as a fleet row.
 *
 * `shortenPodId` is derived by the server and marked read-only, but it is derived from the first
 * eight characters of the id — so the fallback below is the same value rather than a guess.
 */
function toPodRow(pod: Pod, storesByPod: ReadonlyMap<string, number> | null): PodRow {
  const id = pod.id?.id ?? '';
  return {
    id,
    name: pod.name,
    shortId: pod.shortenPodId ?? id.slice(0, 8),
    endpoint: pod.endpoint?.endpoint ?? '',
    endpointType: pod.endpoint?.type ?? '',
    orgId: pod.orgId?.id ?? null,
    // Absent from a successful answer is zero; no answer at all is null.
    stores: storesByPod === null ? null : (storesByPod.get(id) ?? 0),
  };
}
