import {Injectable} from "@angular/core";
import {CrudService} from "../../shared/services/crud.service";
import {Observable} from "rxjs";
import {PageRequest, PageT} from "../../common/BaseTable";

@Injectable({
  providedIn: 'root'
})
export class PodService {
  constructor(
    private crudService: CrudService) {
  }

  listPods(): Observable<Pod[]> {
    return this.crudService.get(`/control-plane/api/v1/pod/list`);
  }

  findAllPods(params:PageRequest): Observable<PageT<Pod>> {
    return this.crudService.get(`/control-plane/api/v1/pod`,params);
  }

  getPod(id: string): Observable<Pod> {
    return this.crudService.get(`/control-plane/api/v1/pod/${id}`);
  }

  create(pod: Pod): Observable<Pod> {
    return this.crudService.post(`/control-plane/api/v1/pod`, pod);
  }

  update(id: string, pod: Pod): Observable<Pod> {
    return this.crudService.put(`/control-plane/api/v1/pod/${id}`, pod);
  }

  delete(id: string): Observable<void> {
    return this.crudService.delete(`/control-plane/api/v1/pod/${id}`);
  }

}


export type Pods = Pod[]

export interface Pod {
  id: PodId
  name: string
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
