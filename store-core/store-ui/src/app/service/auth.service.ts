import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {map, Observable, of} from "rxjs";
import {environment} from '../../environment';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private authUser: AuthUser | undefined;

    constructor(private httpClient: HttpClient) {
/*
        this.authUser = {
          email_verified: false,
          family_name: "",
          given_name: "",
          preferred_username: "",
          sub: "temp"
        }
*/
    }

    getAuthUser(): Observable<AuthUser> {
        if (this.authUser) {
            return of(this.authUser)
        } else {
            return this.httpClient.get<any>(environment.USER_INFO_URL)
                .pipe(map((it: any) => {
                    this.authUser = it.principal.claims;
                    return it;
                }))
        }
    }
}

export interface AuthUser {
    sub: string
    email_verified: boolean,
    preferred_username: string
    given_name: string
    family_name: string
}
