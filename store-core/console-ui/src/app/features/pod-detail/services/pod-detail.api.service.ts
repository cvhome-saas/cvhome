import {Injectable, inject} from '@angular/core';
import {Observable, forkJoin, map, of} from 'rxjs';

import {PodService, type NewPod} from '@api/pod-registry/pod.service';
import {ManagerStoreService} from '@api/tenancy/manager-store.service';
import {OrgService} from '@api/tenancy/org.service';
import {optionalOne} from '@core/http/optional';
import {toOrgRow, toPlatformStoreRow, toPodDetail, type PlatformStoreRow, type PodDetail} from '@models/platform';
import type {Endpoint} from '@models/pod';

/** How many organizations the owner picker offers. See `pods.api.service.ts` for why there is a cap. */
export const ORG_PICKER_LIMIT = 200;

/** One organization, as the owner picker lists it. */
export interface OrgChoice {
  readonly id: string;
  /** The organisation's name, or empty — never the contact email, which reads as a person here. */
  readonly name: string;
}

/** One pod, plus the organizations an operator could hand a new one to. */
export interface PodDetailSnapshot {
  readonly pod: PodDetail | null;
  readonly orgs: readonly OrgChoice[];
}

/**
 * One pod, and the registry operations on it.
 *
 * **`GET /pod/{id}` is the only place the operational state exists.** The paged list answers the
 * routing `Pod`; lifecycle, visibility, region, capacity and health are on `PodView` and are what
 * make this page worth having.
 *
 * The organization list rides along as an **optional** leg: it fills the owner picker, and a create
 * form that can only make a public pod is still a working form — which is what seller-ui's was,
 * since its owner field was free text.
 */
/** The stores on one pod, as the detail's own panel reads them. */
export interface PodStoresSnapshot {
  readonly rows: readonly PlatformStoreRow[];
  readonly totalElements: number;
  readonly totalPages: number;
  /** The term these rows answer, echoed back — the empty state needs to know which query it describes. */
  readonly term: string;
}

@Injectable({providedIn: 'root'})
export class PodDetailApi {
  private readonly pods = inject(PodService);
  private readonly orgs = inject(OrgService);
  private readonly stores = inject(ManagerStoreService);

  /** `id` null on the create route: there is no pod yet, only the picker to fill. */
  load(id: string | null): Observable<PodDetailSnapshot> {
    return forkJoin({
      pod: id ? this.pods.find(id).pipe(map(toPodDetail)) : of(null),
      // Optional: it only fills the owner picker, which is absent on an edit anyway.
      orgs: this.orgs.list(0, ORG_PICKER_LIMIT).pipe(optionalOne()),
    }).pipe(
      map(({pod, orgs}) => ({
        pod,
        orgs: (orgs?.content ?? []).map(toOrgRow).map((org) => ({id: org.id, name: org.name})),
      })),
    );
  }

  /**
   * Registers a pod and answers its id.
   *
   * Only the id, because the create returns the routing `Pod` — lifecycle, visibility and capacity
   * are decided by the server and are not in that response. The page navigates to the detail route,
   * which reads the view; echoing the create's answer would show a pod missing three quarters of
   * what the operator just made.
   */
  /**
   * A page of the stores placed on this pod.
   *
   * **From tenancy, not the registry.** `manager_store.pod_id` is the authoritative link and carries the names,
   * organizations, statuses and billing standings; pod-registry's `pod_store_placement` holds store ids alone and
   * only for stores placed through its outbox. See lessons.md, "Pods — the registry's store count is a mirror,
   * and mirrors drift".
   *
   * Its own call rather than a leg of the page's load: the panel pages, and a pod's routing and health are still
   * worth reading when tenancy is unreachable.
   *
   * The term searches the store's name or its id, server-side — so it narrows every store on the pod rather than
   * the page in the panel.
   */
  loadStores(podId: string, page: number, count: number, term: string): Observable<PodStoresSnapshot> {
    return this.stores.listByPod(podId, page, count, term).pipe(
      map((result) => ({
        rows: (result.content ?? []).map(toPlatformStoreRow),
        totalElements: result.totalElements,
        totalPages: result.totalPages,
        term,
      })),
    );
  }

  create(pod: NewPod): Observable<string> {
    return this.pods.create(pod).pipe(map((created) => created.id?.id ?? ''));
  }

  update(id: string, name: string, endpoint: Endpoint): Observable<void> {
    return this.pods.update(id, {name, endpoint}).pipe(map(() => undefined));
  }

  drain(id: string): Observable<void> {
    return this.pods.drain(id).pipe(map(() => undefined));
  }

  resume(id: string): Observable<void> {
    return this.pods.resume(id).pipe(map(() => undefined));
  }

  delete(id: string): Observable<void> {
    return this.pods.delete(id);
  }
}
