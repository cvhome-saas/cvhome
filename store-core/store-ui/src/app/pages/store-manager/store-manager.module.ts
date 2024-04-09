import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StoreManagerComponent} from './store-manager/store-manager.component';
import {StoreManagerRoutingModule} from "./store-manager-routing.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ManagersComponent} from "./managers/managers.component";
import {SharedModule} from "./shared/shared.module";


@NgModule({
  declarations: [
    StoreManagerComponent,
    ManagersComponent,
  ],
  imports: [
    CommonModule,
    StoreManagerRoutingModule,
    SharedModule
  ]
})
export class StoreManagerModule {
}
