import { Injectable, inject } from "@angular/core";
import {CrudService} from "seller-core";
import {Observable} from "rxjs";
import {PageRequest, PageT} from "seller-core";

/** Base path for pod endpoints. Pods now live in their own service; tenancy no longer has a pod table.
 *  Hoisting this constant in the rename is what made that move a one-line change here. */
export const POD_API_BASE = '/pod-registry/api/v1/pod';

@Injectable({
  providedIn: 'root'
})
export class PodService {
  private crudService = inject(CrudService);


  listPods(): Observable<Pod[]> {
    return this.crudService.get(`${POD_API_BASE}/list`);
  }

  findAllPods(params: PageRequest): Observable<PageT<Pod>> {
    return this.crudService.get(`${POD_API_BASE}`, params);
  }

  getPod(id: string): Observable<Pod> {
    return this.crudService.get(`${POD_API_BASE}/${id}`);
  }

  create(pod: Pod): Observable<Pod> {
    return this.crudService.post(`${POD_API_BASE}`, pod);
  }

  update(id: string, pod: Pod): Observable<Pod> {
    return this.crudService.put(`${POD_API_BASE}/${id}`, pod);
  }

  delete(id: string): Observable<void> {
    return this.crudService.delete(`${POD_API_BASE}/${id}`);
  }

}


export type Pods = Pod[]

export interface Pod {
  id?: PodId
  name: string
  shortenPodId: string
  endpoint: Endpoint
  orgId: ManagerOrgId
}

export interface PodId {
  id: string
}

export interface Endpoint {
  endpoint: string
  type: EndpointType
}

export enum EndpointType {
  EXTERNAL = 'EXTERNAL',
  INTERNAL = 'INTERNAL'
}

export interface ManagerOrgId {
  id: string
}
