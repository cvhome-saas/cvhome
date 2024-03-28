import {Component, OnInit} from '@angular/core';
import {StoreService} from "../../../../../service/store.service";

@Component({
    selector: 'app-manage-store-catalog',
    templateUrl: './manage-store-catalog.component.html',
    styleUrls: ['./manage-store-catalog.component.css']
})
export class ManageStoreCatalogComponent implements OnInit {


    constructor(private storeService: StoreService) {

    }

    ngOnInit(): void {
    }

    ngAfterViewInit(): void {
    }

    onSubmit() {

    }
}
