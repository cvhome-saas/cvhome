import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ChangeGroupRequest, CreateUserRequest, ManagerStoreId, ResetPassword, User} from "../domain/commons";

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private readonly USER_ACCOUNT_BASE_URL: string = '/manager/api/v1/user-account';

    constructor(private httpClient: HttpClient) {
    }


    list(store: ManagerStoreId): Observable<User[]> {
        return this.httpClient.get<User[]>(`${this.USER_ACCOUNT_BASE_URL}/list?store=${store.id}`)
    }

    create(request: CreateUserRequest, store: ManagerStoreId): Observable<User> {
        return this.httpClient.post<User>(`${this.USER_ACCOUNT_BASE_URL}/create?store=${store.id}`, request)
    }

    reset(store: ManagerStoreId, userId: string, password: ResetPassword): Observable<any> {
        return this.httpClient.post(`${this.USER_ACCOUNT_BASE_URL}/reset?store=${store.id}&userId=${userId}`, password)
    }

    delete(store: ManagerStoreId, userId: string): Observable<any> {
        return this.httpClient.delete(`${this.USER_ACCOUNT_BASE_URL}/delete?store=${store.id}&userId=${userId}`)
    }

    enable(store: ManagerStoreId, userId: string): Observable<any> {
        return this.httpClient.post(`${this.USER_ACCOUNT_BASE_URL}/enable?store=${store.id}&userId=${userId}`, {})
    }

    disable(store: ManagerStoreId, userId: string): Observable<any> {
        return this.httpClient.post(`${this.USER_ACCOUNT_BASE_URL}/disable?store=${store.id}&userId=${userId}`, {})
    }

    updateGroups(store: ManagerStoreId, userId: string, request: ChangeGroupRequest): Observable<any> {
        return this.httpClient.post(`${this.USER_ACCOUNT_BASE_URL}/update-groups?store=${store.id}&userId=${userId}`, request)
    }

}
