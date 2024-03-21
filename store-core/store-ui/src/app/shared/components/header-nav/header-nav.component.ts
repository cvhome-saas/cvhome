import {Component, OnInit} from '@angular/core';
import {environment} from "../../../../environment";
import {AuthService, AuthUser} from "../../../service/auth.service";

@Component({
    selector: 'app-header-nav',
    templateUrl: './header-nav.component.html',
    styleUrls: ['./header-nav.component.css']
})
export class HeaderNavComponent implements OnInit {
    accountUrl: string;
    authUser: AuthUser | undefined = undefined;
    loginUrl: string;
    registerUrl: string;


    constructor(private authService: AuthService) {
        this.accountUrl = environment.ACCOUNT_URL;
        this.loginUrl = environment.LOGIN_URL;
        this.registerUrl = environment.REGISTER_URL;
    }

    goAccount() {
        const accountUrl = `${this.accountUrl}?referrer=${environment.CLIENT_APP}`;
        window.location.href = accountUrl;
    }

    ngOnInit(): void {
        this.authService.getAuthUser().subscribe(it => this.authUser = it);
    }

}
