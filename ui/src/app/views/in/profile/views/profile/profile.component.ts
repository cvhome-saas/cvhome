import {Component, OnInit} from '@angular/core';
import {AuthService, AuthUser} from "../../../../../service/auth.service";

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
    authUser: AuthUser | null = null;

    constructor(private authService: AuthService) {
    }

    ngOnInit(): void {
        this.authService.getAuthUser().subscribe(it => {
            this.authUser = it
        })
    }
}
