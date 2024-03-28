import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {Country, Page, Store} from "../../../../../domain/commons";
import {StoreService} from "../../../../../service/store.service";

@Component({
    selector: 'app-store-manager',
    templateUrl: './store-manager.component.html',
    styleUrls: ['./store-manager.component.css']
})
export class StoreManagerComponent implements OnInit {
    stores: Page<Store> = {};
    storeForm: FormGroup;
    countries: Country[] = Object.values(Country);


    constructor(private storeService: StoreService) {
        this.storeForm = new FormGroup({
            name: new FormControl(null, [
                Validators.required,
                Validators.minLength(4),
                Validators.pattern('^[a-zA-Z0-9]+$')
            ]),
            email: new FormControl(null, [
                Validators.required,
                Validators.email
            ]),
            country: new FormControl(this.countries[0], [
                Validators.required
            ])
        });

    }

    ngOnInit(): void {
    }

    ngAfterViewInit(): void {
        this.storeService.list().subscribe(it => this.stores = it);
    }

    onSubmit() {
        if (this.storeForm.valid) {
            let value: any = this.storeForm.value;
            this.storeService.create({
                name: value.name,
                email: {
                    email: value.email
                },
                country: value.country
            }).subscribe((it: Store) => {
                if (!this.stores.content) this.stores.content = []
                this.stores.content.push(it);
                this.storeForm.reset({"country": this.countries[0]});
            });
        }
    }
}
