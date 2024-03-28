import {Component, OnInit} from '@angular/core';
import {Store, StoreService} from "../../../../../service/store.service";
import {FormControl, FormGroup, Validators} from "@angular/forms";

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
