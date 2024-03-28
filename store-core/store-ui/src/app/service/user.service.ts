import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CreateStoreRequest, ManagerStoreId, ResetPassword, User} from "../domain/commons";

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private readonly USER_ACCOUNT_BASE_URL: string = '/manager/api/v1/user-account';

    constructor(private httpClient: HttpClient) {
    }

    create(request: CreateStoreRequest): Observable<User> {
        return this.httpClient.post<User>(`${this.USER_ACCOUNT_BASE_URL}/create`, request)
    }

    list(storeId: ManagerStoreId): Observable<User[]> {
        return this.httpClient.get<User[]>(`${this.USER_ACCOUNT_BASE_URL}/list?storeId=${storeId.id}`)
    }

    reset(storeId: ManagerStoreId, userId: string, password: ResetPassword): Observable<any> {
        return this.httpClient.post(`${this.USER_ACCOUNT_BASE_URL}/reset?storeId=${storeId.id}&userId=${userId}`, password)
    }

    delete(storeId: ManagerStoreId, userId: string): Observable<any> {
        return this.httpClient.delete(`${this.USER_ACCOUNT_BASE_URL}/delete?storeId=${storeId.id}&userId=${userId}`)
    }

    enable(storeId: ManagerStoreId, userId: string): Observable<any> {
        return this.httpClient.post(`${this.USER_ACCOUNT_BASE_URL}/enable?storeId=${storeId.id}&userId=${userId}`, {})
    }

    disable(storeId: ManagerStoreId, userId: string): Observable<any> {
        return this.httpClient.post(`${this.USER_ACCOUNT_BASE_URL}/disable?storeId=${storeId.id}&userId=${userId}`, {})
    }

}
