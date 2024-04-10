import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {StoreManagerComponent} from "./views/store-manager/store-manager.component";
import {ManageStoreComponent} from "./views/manage-store/manage-store.component";
import {ManageStoreLandingComponent} from "./views/manage-store-landing/manage-store-landing.component";
import {ManageStoreUsersComponent} from "./views/manage-store-users/manage-store-users.component";
import {ManageStoreCatalogComponent} from "./views/manage-store-catalog/manage-store-catalog.component";
import {ManageStoreOrdersComponent} from "./views/manage-store-orders/manage-store-orders.component";

const routes: Routes = [
    {path: '', component: StoreManagerComponent},
    {
        path: ':store', component: ManageStoreComponent, children: [
            {path: '', component: ManageStoreLandingComponent},
            {path: 'users', component: ManageStoreUsersComponent},
            {path: 'orders', component: ManageStoreOrdersComponent},
            {path: 'catalog', component: ManageStoreCatalogComponent},
        ]
    },
];

// configures NgModule imports and exports
@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class StoreManagerRoutingModule {
}
