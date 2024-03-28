import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class StoreService {
    private readonly STORE_BASE_URL: string = '/manager/api/v1/store-manager';

    constructor(private httpClient: HttpClient) {
    }

    create(request: CreateStoreRequest): Observable<Store> {
        return this.httpClient.post<Store>(`${this.STORE_BASE_URL}/create`, request)
    }

    findAllStores(): Observable<Page<Store>> {
        return this.httpClient.post<Page<Store>>(`${this.STORE_BASE_URL}/list`, {})
    }

}

export interface StoreId {
    id: string;
}

export interface IdentityId {
    id: string;
}

export interface CreateStoreRequest {
    name?: string;
    country?: Country;
    email?: Email;
}

export interface Store {
    id: StoreId;
    name: string;
    owner: IdentityId;
}

export interface Page<T> {
    content?: T[];
}

export enum Country {
    EG = 'EG',
    KSA = 'KSA',
    UAE = 'UAE'
}

export interface Email {
    email: string;
}