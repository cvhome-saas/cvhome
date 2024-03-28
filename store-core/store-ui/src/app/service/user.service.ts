import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CreateStoreRequest, Page, Store} from "../domain/commons";

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private readonly USER_ACCOUNT_BASE_URL: string = '/manager/api/v1/user-account';

    constructor(private httpClient: HttpClient) {
    }

    create(request: CreateStoreRequest): Observable<Store> {
        return this.httpClient.post<Store>(`${this.USER_ACCOUNT_BASE_URL}/create`, request)
    }

    list(): Observable<Page<Store>> {
        return this.httpClient.post<Page<Store>>(`${this.USER_ACCOUNT_BASE_URL}/list`, {})
    }


}
