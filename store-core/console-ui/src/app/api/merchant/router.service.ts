/** Ported from seller-ui/projects/seller-core/stores/src/lib/services/store.service.ts. */
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';

import {CrudService} from '@core/http/crud.service';
import type {ManagerStoreDomain} from '@models/merchant';

/**
 * Ported from seller-ui/projects/seller-core/stores/src/lib/services/store.service.ts
 * (`getAllocations`, `allocateDomain`, `removeDomain`), verified against
 * `merchant-service/api/v1/RouterController.java`.
 *
 * The hostnames a storefront answers on.
 *
 * The merchant pod owns this, not tenancy: `MerchantRoutingService` keeps the domain → store map that
 * the edge reads to decide which store a request belongs to. Tenancy's `RouterApi` is a different and
 * complementary thing — it says which *pod* a store lives on, which is what a CNAME has to point at.
 * `@api/tenancy/saas.service.ts` covers that half.
 *
 * The public half of `RouterController` — `public/ask-for-tls` and `public/lookup-by-domain` — is the
 * edge's own business and is deliberately not ported.
 */
const ROUTER_API_BASE = '/spg/merchant/api/v1/router';

@Injectable({providedIn: 'root'})
export class MerchantRouterService {
  private readonly crudService = inject(CrudService);

  /**
   * Every domain allocated to the current store.
   *
   * Answers with a `Set<ManagerStoreDomain>`, so the order is the server's and means nothing — the
   * console sorts. A store always has one `SUB_DOMAIN`, created with it, and may have any number of
   * `CUSTOM_DOMAIN`s; the console's fixture allowed exactly one of each, which was never the contract.
   */
  allocations(): Observable<ManagerStoreDomain[]> {
    return this.crudService.get(`${ROUTER_API_BASE}/private/allocates`);
  }

  /**
   * Points a hostname at the current store.
   *
   * `domain` is a query parameter rather than a body: the controller binds it as a `Domain` record
   * from the request parameters, and a body is not read at all. seller-core sent `{store}` as a POST
   * body for the same call, which the server ignored — the request context is what scopes it.
   */
  allocate(domain: string): Observable<void> {
    return this.crudService.post(`${ROUTER_API_BASE}/private/allocate`, null, {domain});
  }

  remove(domain: string): Observable<void> {
    return this.crudService.delete(`${ROUTER_API_BASE}/private/remove`, {domain});
  }
}
