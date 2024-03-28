import {Component, OnInit} from '@angular/core';
import {StoreService} from "../../../../../service/store.service";

@Component({
    selector: 'app-manage-store-users',
    templateUrl: './manage-store-users.component.html',
    styleUrls: ['./manage-store-users.component.css']
})
export class ManageStoreUsersComponent implements OnInit {


    constructor(private storeService: StoreService) {

    }

    ngOnInit(): void {
    }

    ngAfterViewInit(): void {
    }

    onSubmit() {

    }
}
