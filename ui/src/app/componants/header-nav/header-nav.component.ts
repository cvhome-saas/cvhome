import {Component} from '@angular/core';
import {environment} from "../../../environment";

@Component({
    selector: 'app-header-nav',
    templateUrl: './header-nav.component.html',
    styleUrls: ['./header-nav.component.css']
})
export class HeaderNavComponent {
    accountUrl: string;

    constructor() {
        this.accountUrl = environment.ACCOUNT_URL;
    }

    goAccount() {
        const accountUrl = `${this.accountUrl}?referrer=${environment.CLIENT_APP}`;
        window.location.href = accountUrl;
    }
}
