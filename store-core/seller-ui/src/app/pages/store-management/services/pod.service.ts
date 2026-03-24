import {Injectable} from "@angular/core";
import {CrudService} from "../../shared/services/crud.service";
import {Observable} from "rxjs";

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
  type: string
}

export interface ManagerOrgId {
  id: string
}

