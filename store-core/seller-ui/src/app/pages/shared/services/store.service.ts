import {Injectable, inject} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, of} from "rxjs";
import {CreateStoreRequest, Page, ManagerStore} from "../models/commons";
import {catchError} from "rxjs/operators";
import {environment} from "../../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class StoreService {
  private readonly httpClient = inject(HttpClient);
  private readonly STORE_MANAGER_BASE_URL: string = '/control-plane/api/v1/store-manager';

  create(request: CreateStoreRequest): Observable<ManagerStore> {
    return this.httpClient.post<ManagerStore>(`${this.STORE_MANAGER_BASE_URL}/create`, request)
  }

  list(): Observable<Page<ManagerStore>> {
    const defaultPageOnError: Page<ManagerStore> = {
      content: [{
        id: {
          id: environment.defaultStore
        },
        name: "default",
        provisioningState: "SUCCESSFULLY_PROVISIONING",
        orgId: {
          id: "org-id"
        },
        podId: {
          id: "pod-id"
        }
      }],
    };
    return this.httpClient.post<Page<ManagerStore>>(`${this.STORE_MANAGER_BASE_URL}/list`, {})
      .pipe(catchError(() => of(defaultPageOnError)));
  }

}
