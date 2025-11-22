import {Injectable} from '@angular/core';

import {CrudService} from '../../shared/services/crud.service';
import {Observable} from 'rxjs';
import {Org} from "../model/org";

@Injectable({
  providedIn: 'root'
})
export class OrgService {

  constructor(
    private crudService: CrudService) {
  }


  getListOfOrg(params): Observable<any> {
    return this.crudService.get('control-plane/api/v1/org-manager/find-all', params);
  }

  getOrg(params): Observable<Org> {
    return this.crudService.get('control-plane/api/v1/org-manager/find-one?id=' + params);
  }

  getSubscriptionPlans(): Observable<string[]> {
    return this.crudService.get('control-plane/api/v1/subscription-plan/public/list');
  }

  updateOrg(id: string, orgData: any) {
    return this.crudService.put('control-plane/api/v1/org-manager/update?id=' + id, orgData);
  }

  createOrg(orgData: any) {
    return this.crudService.post('control-plane/api/v1/org-manager/create', orgData);
  }

  changeOrgPassword(id: string, passwords: any) {
    return this.crudService.post('control-plane/api/v1/org-manager/change-password?id=' + id, passwords);
  }

  getOrgStoresList(params: any): Observable<any> {
    return this.crudService.get('control-plane/api/v1/org-manager/stores', params);
  }
}
