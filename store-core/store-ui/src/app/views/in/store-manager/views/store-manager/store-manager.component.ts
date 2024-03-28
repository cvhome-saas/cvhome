import {Component, OnInit} from '@angular/core';
import {Store, StoreService} from "../../../../../service/store.service";
import {FormControl, FormGroup, Validators} from "@angular/forms";

@Component({
    selector: 'app-store-manager',
    templateUrl: './store-manager.component.html',
    styleUrls: ['./store-manager.component.css']
})
export class StoreManagerComponent implements OnInit {
    stores: Store[] = [];
    storeForm: FormGroup;


    constructor(private storeService: StoreService) {
        this.storeForm = new FormGroup({
            name: new FormControl(null, [
                Validators.required,
                Validators.minLength(4),
                Validators.pattern('^[a-zA-Z0-9]+$')
            ]),
        });

    }

    ngOnInit(): void {
    }

    ngAfterViewInit(): void {
        this.storeService.findAllStores().subscribe(it => this.stores = it);
    }

    onSubmit() {
        if (this.storeForm.valid) {
            let value: any = this.storeForm.value;
            this.storeService.create({
                name: value.name
            }).subscribe((it: Store) => {
                this.stores.push(it);
                this.storeForm.reset();
            });
        }
    }
}
