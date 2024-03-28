import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpClientModule} from "@angular/common/http";
import {StoreManagerComponent} from "./views/store-manager/store-manager.component";
import {StoreManagerRoutingModule} from "./store-manager.routing.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ManageStoreComponent} from "./views/manage-store/manage-store.component";
import {ManageStoreCatalogComponent} from "./views/manage-store-catalog/manage-store-catalog.component";
import {ManageStoreUsersComponent} from "./views/manage-store-users/manage-store-users.component";
import {ManageStoreLandingComponent} from "./views/manage-store-landing/manage-store-landing.component";
import {ManageStoreOrdersComponent} from "./views/manage-store-orders/manage-store-orders.component";
import {RouterLink} from "@angular/router";

@NgModule({
    imports: [
        CommonModule, HttpClientModule, StoreManagerRoutingModule, FormsModule, ReactiveFormsModule, RouterLink
    ],
    declarations: [StoreManagerComponent,
        ManageStoreComponent,
        ManageStoreCatalogComponent,
        ManageStoreOrdersComponent,
        ManageStoreUsersComponent,
        ManageStoreLandingComponent]
})
export class StoreManagerModule {
}
