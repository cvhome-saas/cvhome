import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@cvhome-saas/ui-kit';
import type {SpringPage} from '@cvhome-saas/ui-kit';
import type {Endpoint, Pod, PodView} from '@models/pod';

export const POD_API_BASE = '/pod-registry/api/v1/pod';

@Injectable({providedIn: 'root'})
export class PodService {
  private readonly crudService = inject(CrudService);

  /**
   * The pods this caller may place a store into.
   *
   * **Usually empty.** `PodApi.listPods` gives a super admin every pod and an org admin only its *own
   * private* pods; the shared pods a normal merchant is actually placed into are matched internally by
   * `listPlaceablePublicPods()`, which is exposed on no endpoint. So callers must treat an empty list as
   * the normal case and let the registry choose — see lessons.md, "Shell — no merchant-readable list of
   * placeable pods".
   */
  list(): Observable<Pod[]> {
    return this.crudService.get(`${POD_API_BASE}/list`);
  }

  /**
   * The same pods, paged — what the platform fleet screen binds to.
   *
   * `count`, not Spring's `size`: `pod-registry-service` depends on `store-commons:autoconfigure`,
   * whose `ServletWebConfig` renames the page-size parameter platform-wide.
   *
   * **It answers `Pod`, not `PodView`.** Lifecycle, visibility, region, capacity and health live on
   * the view and are reachable only per id, so none of them can be a column — one request per row is
   * not a column. See lessons.md, "Pods — the paged list returns the routing record, not the view".
   *
   * `q` searches the name and the endpoint, case-insensitively and as a substring — the two things an
   * operator has in hand when they come looking, unlike the ObjectId nobody reads out. Omitted rather
   * than sent empty, so a cleared box and an untouched one are one request.
   */
  page(page: number, count: number, term = ''): Observable<SpringPage<Pod>> {
    const search = term.trim();
    return this.crudService.get(POD_API_BASE, {page, count, q: search || undefined});
  }

  /** One pod, as the registry knows it: lifecycle, visibility, region, capacity and last health. */
  find(id: string): Observable<PodView> {
    return this.crudService.get(`${POD_API_BASE}/${id}`);
  }

  /**
   * Registers a pod.
   *
   * Visibility is **derived, not sent**: `PodEntity.newEntity` writes `PRIVATE` when an org is named
   * and `PUBLIC` when one is not, and the lifecycle state is forced to `ACTIVE`. Creation is
   * therefore the one moment either is decided — see lessons.md, "Pods — visibility, region,
   * capacity and owner cannot be edited".
   *
   * A duplicate name comes back as `DuplicatePodNameException`; the unique constraint is the
   * authority behind it, so two operators racing get one 409 rather than two rows.
   */
  create(pod: NewPod): Observable<Pod> {
    return this.crudService.post(POD_API_BASE, {
      name: pod.name,
      endpoint: pod.endpoint,
      orgId: pod.orgId ? {id: pod.orgId} : null,
    });
  }

  /**
   * Renames a pod and repoints its endpoint.
   *
   * **Only those two.** `PodServiceImpl.update` reads `name` and `endpoint` off the body and ignores
   * everything else, so visibility, region, capacity and owner cannot be changed once the row exists.
   * The console renders them disabled rather than sending values the server drops — see lessons.md,
   * "Pods — visibility, region, capacity and owner cannot be edited".
   */
  update(id: string, pod: {name: string; endpoint: Endpoint}): Observable<Pod> {
    return this.crudService.put(`${POD_API_BASE}/${id}`, {name: pod.name, endpoint: pod.endpoint});
  }

  /**
   * Stops new stores being placed here, without touching the ones already on it.
   *
   * The safe counterpart to {@link delete}: a drained pod keeps its gateway route and keeps serving
   * its tenants. Draining an already-drained pod is a no-op that still writes an audit row.
   */
  drain(id: string): Observable<PodView> {
    return this.crudService.post(`${POD_API_BASE}/${id}/drain`, null);
  }

  /** Returns a drained pod to rotation. */
  resume(id: string): Observable<PodView> {
    return this.crudService.post(`${POD_API_BASE}/${id}/resume`, null);
  }

  /**
   * Removes a pod from the registry.
   *
   * **It checks nothing.** `PodApi.delete`'s own doc comment says there is no foreign key to stop it
   * — tenancy owns `manager_store.pod_id` in a different schema — so deleting a populated pod orphans
   * every store on it. The console puts this behind a typed confirmation and points at drain.
   *
   * Nothing can answer "which stores are on this pod" either; `pod_store_placement` is the table that
   * would, and no endpoint reads it. See lessons.md, "Pods — no way to see which stores are on a pod".
   */
  delete(id: string): Observable<void> {
    return this.crudService.delete(`${POD_API_BASE}/${id}`);
  }
}

/** What the create form sends. `orgId` is the owning organization's id, or null for a public pod. */
export interface NewPod {
  readonly name: string;
  readonly endpoint: Endpoint;
  readonly orgId: string | null;
}
