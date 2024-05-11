import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, of} from "rxjs";
import {CreateStoreRequest, ManagerStoreId, Page, Store} from "../domain/commons";

@Injectable({
  providedIn: 'root'
})
export class StoreService {
  private readonly STORE_MANAGER_BASE_URL: string = '/manager/api/v1/store-manager';

  constructor(private httpClient: HttpClient) {
  }

  create(request: CreateStoreRequest): Observable<Store> {
    return this.httpClient.post<Store>(`${this.STORE_MANAGER_BASE_URL}/create`, request)
  }

  list(): Observable<Page<Store>> {
/*
    return of({
      content: [{
        id: {
          id: "65f023632bc46470c104b76f"
        },
        name: "default"
      }]
    })
*/
    return this.httpClient.post<Page<Store>>(`${this.STORE_MANAGER_BASE_URL}/list`, {})
  }

}
