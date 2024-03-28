import {Component, OnInit} from '@angular/core';
import {StoreService} from "../../../../../service/store.service";

@Component({
    selector: 'app-manage-store',
    templateUrl: './manage-store.component.html',
    styleUrls: ['./manage-store.component.css']
})
export class ManageStoreComponent implements OnInit {


    constructor(private storeService: StoreService) {

    }

    ngOnInit(): void {
    }

    ngAfterViewInit(): void {
    }

    onSubmit() {

    }
}
