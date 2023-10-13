import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class StoreService {
  private readonly STORE_BASE_URL: string = '/store/api/v1/store';

  constructor(private httpClient: HttpClient) {
  }

  create(request: CreateStoreRequest): Observable<Store> {
    return this.httpClient.post<Store>(`${this.STORE_BASE_URL}`, request)
  }

  findAllStores(): Observable<Store[]> {
    return this.httpClient.get<Store[]>(`${this.STORE_BASE_URL}/me`)
  }

}

export interface StoreId {
  id: string;
}

export interface IdentityId {
  id: string;
}

export interface CreateStoreRequest {
  name: string;
}

export interface Store {
  id: StoreId;
  name: string;
  owner: IdentityId;
}
