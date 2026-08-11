import { Injectable, inject } from '@angular/core';

import {CrudService} from 'seller-core';
import {Observable, map} from 'rxjs';
import {Org} from "../model/org";
import {SpringPage, StorePageRequest} from 'seller-core';
import {ManagerStore} from 'seller-core';
import {User} from 'seller-core';

/** Base path for tenancy's org-manager endpoints. */
export const ORG_MANAGER_API_BASE = '/tenancy/api/v1/org-manager';

@Injectable({
  providedIn: 'root'
})
export class OrgService {
  private readonly crudService = inject(CrudService);


  getListOfOrg(params: StorePageRequest): Observable<SpringPage<Org>> {
    return this.crudService.get(`${ORG_MANAGER_API_BASE}/find-all`, params);
  }

  getOrg(id: string): Observable<Org> {
    return this.crudService.get(`${ORG_MANAGER_API_BASE}/find-one?id=` + id);
  }

  /**
   * Plan codes, from billing's public catalog.
   *
   * Repointed when the org-level subscriptions were retired; the endpoint this used to call no longer
   * exists. Note the concept it serves does not either: a plan now belongs to a store, not to an org, so the
   * org-management screens that offer this list are choosing something that is no longer applied anywhere. Kept
   * working rather than silently 404-ing, but those screens need a product decision, not a client fix.
   */
  getSubscriptionPlans(): Observable<string[]> {
    return this.crudService
      .get<{code: string}[]>('billing/api/v1/plan/public/plans')
      .pipe(map((plans) => plans.map((plan) => plan.code)));
  }

  /** No matching controller was found for a PUT org-manager/update endpoint
   *  — typed from the request shape only, response left unverified. */
  updateOrg(id: string, orgData: Record<string, unknown>): Observable<unknown> {
    return this.crudService.put(`${ORG_MANAGER_API_BASE}/update?id=` + id, orgData);
  }

  createOrg(orgData: Record<string, unknown>): Observable<User> {
    return this.crudService.post(`${ORG_MANAGER_API_BASE}/create`, orgData);
  }

  changeOrgPassword(id: string, passwords: Record<string, unknown>): Observable<void> {
    return this.crudService.post(`${ORG_MANAGER_API_BASE}/change-password?id=` + id, passwords);
  }

  getOrgStoresList(params: {id: string} & Partial<StorePageRequest>): Observable<SpringPage<ManagerStore>> {
    return this.crudService.get(`${ORG_MANAGER_API_BASE}/stores`, params);
  }
}
