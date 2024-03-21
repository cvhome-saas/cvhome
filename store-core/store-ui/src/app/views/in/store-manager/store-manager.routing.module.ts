import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {StoreManagerComponent} from "./views/store-manager/store-manager.component";

const routes: Routes = [
    {path: '', component: StoreManagerComponent},
];

// configures NgModule imports and exports
@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class StoreManagerRoutingModule {
}
