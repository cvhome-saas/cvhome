import {Component} from '@angular/core';

@Component({
    selector: 'app-stores-settings',
    templateUrl: './stores-settings.component.html',
    styleUrls: ['./stores-settings.component.css']
})
export class StoresSettingsComponent /*implements AfterViewInit*/ {
/*    stores: Store[] = [];
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
    }*/
}
