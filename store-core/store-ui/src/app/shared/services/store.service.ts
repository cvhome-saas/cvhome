import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, of} from "rxjs";
import {CreateStoreRequest, Page, Store} from "../models/commons";
import {catchError} from "rxjs/operators";
import {environment} from "../../../environments/environment";

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
    const defaultPageOnError: Page<Store> = {
      content: [{
        id: {
          id: environment.defaultStore
        },
        name: "default"
      }],
    };
    return this.httpClient.post<Page<Store>>(`${this.STORE_MANAGER_BASE_URL}/list`, {})
      .pipe(catchError(error => of(defaultPageOnError)));
  }

}
