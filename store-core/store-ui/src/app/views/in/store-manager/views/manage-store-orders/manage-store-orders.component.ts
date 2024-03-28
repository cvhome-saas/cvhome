import {Component, OnInit} from '@angular/core';
import {Store, StoreService} from "../../../../../service/store.service";
import {FormControl, FormGroup, Validators} from "@angular/forms";

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
