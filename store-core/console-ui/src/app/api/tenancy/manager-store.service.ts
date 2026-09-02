import {Injectable, inject} from '@angular/core';
import {Observable, map} from 'rxjs';

import {CrudService} from '@cvhome-saas/ui-kit';
import type {SpringPage} from '@cvhome-saas/ui-kit';
import type {MerchantStore} from '@models/merchant';
import type {CreateStoreRequest, EntityExists, ManagerStore, PodStoreCount} from '@models/tenancy';

/**
 * The stores an operator can reach, and how one is made.
 *
 * The two seller-core services this merges were split for historical reasons — `ManagerStoreService` held
 * `list` and a `create` that posts to `/store-manager/create`, a path **`StoreManagerApi` does not map**.
 * That method is dead code and is deliberately not ported; the create that works is `private/store`, which
 * lived on the other service. Merging them means there is one answer to "how do I make a store".
 */
export const STORE_MANAGER_API_BASE = '/tenancy/api/v1/store-manager';

@Injectable({providedIn: 'root'})
export class ManagerStoreService {
  private readonly crudService = inject(CrudService);

  /**
   * Every store the caller may reach, confined to their org by the server.
   *
   * A POST because it carries a `ListManagerStoreQuery` filter body; an empty one means "no filter".
   * Unpaged in practice — the rail shows all of them — so the page envelope is unwrapped here rather
   * than leaked to callers who have no paging control to drive it.
   *
   * `catchError` is deliberately absent. seller-core once defaulted this to a fabricated page holding one
   * invented store, which meant a tenancy outage silently scoped every later request to a store that does
   * not exist. The failure reaches the caller, which shows it.
   */
  list(): Observable<ManagerStore[]> {
    return this.crudService
      .post<SpringPage<ManagerStore>>(`${STORE_MANAGER_API_BASE}/list`, {})
      .pipe(map((page) => page.content ?? []));
  }

  /**
   * A page of the stores placed on one pod.
   *
   * **Tenancy is the authority here, not the registry.** `manager_store.pod_id` is the fact the platform runs
   * on; pod-registry's `pod_store_placement` is a copy it maintains from tenancy's outbox and knows only about
   * stores placed through it. This also carries the names, orgs, statuses and billing standings the registry's
   * copy does not have.
   *
   * Scoped by the caller's identity like every other listing — the pod filter narrows what they may already see,
   * so an org admin gets only their own stores on the pod and a super admin gets all of them.
   *
   * The page envelope is kept, unlike `list` above, because this one pages.
   *
   * `term` searches the store's **name or its id**, case-insensitively and as a substring. That
   * predicate used to be an equality on the name alone — a lookup rather than a search, and one no
   * caller ever passed; the pod screen's search box is what made it worth being real. The id is in
   * it because a store id here is a readable handle (`ORG1-STORE1`), not an opaque ObjectId.
   */
  listByPod(pod: string, page: number, count: number, term = ''): Observable<SpringPage<ManagerStore>> {
    const search = term.trim();
    return this.crudService.post(
      `${STORE_MANAGER_API_BASE}/list`,
      {pod: {id: pod}, name: search || null},
      {page, count},
    );
  }

  /**
   * How many stores sit on each pod, platform-wide.
   *
   * Super-admin only, and one request rather than one per pod: the fleet table wants every count at once.
   *
   * **A pod with no stores is absent from the answer** rather than present as zero — the query groups, and there
   * is nothing to group — so callers must read a missing pod as none placed rather than as unknown.
   */
  storesPerPod(): Observable<PodStoreCount[]> {
    return this.crudService.get(`${STORE_MANAGER_API_BASE}/stores-per-pod`);
  }

  /**
   * One store's current row, which is how provisioning progress is observed — there is no progress
   * endpoint, only this state. Store-scoped, so the caller must pass the id explicitly: the store being
   * polled is usually not the one the request context would stamp.
   */
  storeInfo(store: string): Observable<ManagerStore> {
    return this.crudService.get(`${STORE_MANAGER_API_BASE}/store-info`, {store});
  }

  /**
   * The store as the *merchant* service describes it — trading name, registered address, contact
   * email and phone, logo. `storeInfo` above answers with tenancy's own row instead, which carries
   * none of that.
   *
   * Tenancy does not hold this: `StoreManagerApi.getStoreDetailed` forwards to the store's pod and
   * merges `pod` into the answer, which is why the return type is the pod's `ReadableMerchantStore`
   * rather than `ManagerStoreDto`. Store-scoped by path, so the id is passed explicitly.
   */
  getStoreDetail(store: string): Observable<MerchantStore> {
    return this.crudService.get(`${STORE_MANAGER_API_BASE}/private/store/${store}`);
  }

  /**
   * Creates a store and returns it already carrying its assigned pod.
   *
   * Provisioning is asynchronous: this answers as soon as the row exists, with
   * `provisioningState` still in progress. Poll `storeInfo` for the rest.
   */
  create(request: CreateStoreRequest): Observable<ManagerStore> {
    return this.crudService.post(`${STORE_MANAGER_API_BASE}/private/store`, request);
  }

  /**
   * Whether a store name is taken. Names are unique platform-wide, so this necessarily reports on names
   * outside the caller's org — it is the create form's pre-flight check, and the server restricts it to
   * principals who can actually create a store so it cannot be used to enumerate other tenants.
   */
  nameExists(name: string): Observable<boolean> {
    return this.crudService
      .get<EntityExists>(`${STORE_MANAGER_API_BASE}/private/store/unique`, {name})
      .pipe(map((answer) => answer.exists));
  }

  /**
   * The themes a storefront may run, already filtered to the ones actually built —
   * `Theme.getImplementedThemes()` drops the ten placeholders the enum also declares. Public
   * because the create-store form needs it before a store exists to be scoped to.
   */
  themes(): Observable<string[]> {
    return this.crudService.get(`${STORE_MANAGER_API_BASE}/public/themes`);
  }

  /** The palettes a storefront may run. Thirty values, so the settings form renders a select. */
  colorThemes(): Observable<string[]> {
    return this.crudService.get(`${STORE_MANAGER_API_BASE}/public/color-themes`);
  }

  /**
   * The storefront footer's link providers — `commons` `SocialProvider`, which is a different and
   * longer list than cua's identically named enum for shopper sign-in.
   */
  socialLinkProviders(): Observable<string[]> {
    return this.crudService.get(`${STORE_MANAGER_API_BASE}/public/social-links-providers`);
  }
}
