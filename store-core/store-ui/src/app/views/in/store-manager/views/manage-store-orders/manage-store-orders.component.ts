import {Component, OnInit} from '@angular/core';
import {StoreService} from "../../../../../service/store.service";

@Component({
    selector: 'app-manage-store-orders',
    templateUrl: './manage-store-orders.component.html',
    styleUrls: ['./manage-store-orders.component.css']
})
export class ManageStoreOrdersComponent implements OnInit {


    constructor(private storeService: StoreService) {

    }

    ngOnInit(): void {
    }

    ngAfterViewInit(): void {
    }

    onSubmit() {

    }
}
