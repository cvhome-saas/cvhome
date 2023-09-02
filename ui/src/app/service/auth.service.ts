import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {AuthUser} from "../model/auth-user";
import {map, Observable, of} from "rxjs";
import {environment} from '../../environment';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private authUser: AuthUser | undefined;

    constructor(private httpClient: HttpClient) {
    }

    getAuthUser(): Observable<AuthUser> {
        if (this.authUser) {
            return of(this.authUser)
        } else {
            return this.httpClient.get<AuthUser>(environment.USER_INFO_URL)
                .pipe(map((it: AuthUser) => {
                    this.authUser = it;
                    return it;
                }))
        }
    }
}
