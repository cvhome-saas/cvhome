import { Injectable, inject } from '@angular/core';

import {CrudService} from 'seller-core';
import {Observable} from 'rxjs';
import {Org} from "../model/org";
import {SpringPage, StorePageRequest} from 'seller-core';
import {ManagerStore} from 'seller-core';
import {User} from 'seller-core';

@Injectable({
  providedIn: 'root'
})
export class OrgService {
  private readonly crudService = inject(CrudService);


  getListOfOrg(params: StorePageRequest): Observable<SpringPage<Org>> {
    return this.crudService.get('control-plane/api/v1/org-manager/find-all', params);
  }

  getOrg(id: string): Observable<Org> {
    return this.crudService.get('control-plane/api/v1/org-manager/find-one?id=' + id);
  }

  getSubscriptionPlans(): Observable<string[]> {
    return this.crudService.get('control-plane/api/v1/subscription-plan/public/list');
  }

  /** No matching controller was found for a PUT org-manager/update endpoint
   *  — typed from the request shape only, response left unverified. */
  updateOrg(id: string, orgData: Record<string, unknown>): Observable<unknown> {
    return this.crudService.put('control-plane/api/v1/org-manager/update?id=' + id, orgData);
  }

  createOrg(orgData: Record<string, unknown>): Observable<User> {
    return this.crudService.post('control-plane/api/v1/org-manager/create', orgData);
  }

  changeOrgPassword(id: string, passwords: Record<string, unknown>): Observable<void> {
    return this.crudService.post('control-plane/api/v1/org-manager/change-password?id=' + id, passwords);
  }

  getOrgStoresList(params: {id: string} & Partial<StorePageRequest>): Observable<SpringPage<ManagerStore>> {
    return this.crudService.get('control-plane/api/v1/org-manager/stores', params);
  }
}
