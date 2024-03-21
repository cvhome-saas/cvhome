import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpClientModule} from "@angular/common/http";
import {StoreManagerComponent} from "./views/store-manager/store-manager.component";
import {StoreManagerRoutingModule} from "./store-manager.routing.module";

@NgModule({
    imports: [
        CommonModule, HttpClientModule, StoreManagerRoutingModule
    ],
    declarations: [StoreManagerComponent]
})
export class StoreManagerModule {
}
