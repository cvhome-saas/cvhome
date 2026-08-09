import {Injectable, inject} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CreateStoreRequest, Page, ManagerStore} from "../models/commons";

@Injectable({
  providedIn: 'root'
})
export class ManagerStoreService {
  private readonly httpClient = inject(HttpClient);
  private readonly STORE_MANAGER_BASE_URL: string = '/control-plane/api/v1/store-manager';

  create(request: CreateStoreRequest): Observable<ManagerStore> {
    return this.httpClient.post<ManagerStore>(`${this.STORE_MANAGER_BASE_URL}/create`, request)
  }

  /**
   * Was `catchError(() => of(defaultPageOnError))`, which fabricated a page containing one invented
   * "default" store. On a control-plane outage the seller silently got a store that does not exist, and
   * every query afterwards was scoped to it — a wrong answer presented as a right one. The failure now
   * reaches the caller, which shows it.
   */
  list(): Observable<Page<ManagerStore>> {
    return this.httpClient.post<Page<ManagerStore>>(`${this.STORE_MANAGER_BASE_URL}/list`, {});
  }
}
