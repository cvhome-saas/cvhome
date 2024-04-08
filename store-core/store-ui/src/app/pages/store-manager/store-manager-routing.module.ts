import {NgModule} from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {StoreManagerComponent} from "./store-manager/store-manager.component";
import {ManageStoreUsersComponent} from "./manage-store-users/manage-store-users.component";

const routes: Routes = [
  {path: '', component: StoreManagerComponent},
  {path: 'managers', component: ManageStoreUsersComponent},
  {path: 'category', loadChildren: () => import("./catalog/category/category.module").then(m => m.CategoryModule)},
  {path: 'products', loadChildren: () => import("./catalog/products/products.module").then(m => m.ProductsModule)}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class StoreManagerRoutingModule {
}
