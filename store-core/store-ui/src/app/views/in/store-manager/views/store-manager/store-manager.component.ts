import {Component, OnInit} from '@angular/core';
import {AuthService, AuthUser} from "../../../../../service/auth.service";

@Component({
    selector: 'app-store-manager',
    templateUrl: './store-manager.component.html',
    styleUrls: ['./store-manager.component.css']
})
export class StoreManagerComponent implements OnInit {

    constructor() {
    }

    ngOnInit(): void {
    }
}
