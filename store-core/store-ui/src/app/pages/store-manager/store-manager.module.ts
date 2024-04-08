import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StoreManagerComponent} from './store-manager/store-manager.component';
import {StoreManagerRoutingModule} from "./store-manager-routing.module";
import {
  NbButtonModule,
  NbCardModule,
  NbInputModule,
  NbOptionModule,
  NbRadioModule,
  NbSelectModule
} from "@nebular/theme";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ManageStoreUsersComponent} from "./manage-store-users/manage-store-users.component";
import {StoreSelectorComponent} from "./shared/store-selector/store-selector.component";


@NgModule({
  declarations: [
    StoreManagerComponent,
    ManageStoreUsersComponent,
    StoreSelectorComponent
  ],
  imports: [
    CommonModule,
    FormsModule, ReactiveFormsModule,
    StoreManagerRoutingModule,
    NbCardModule, NbButtonModule, NbInputModule, NbRadioModule, NbOptionModule, NbSelectModule
  ]
})
export class StoreManagerModule {
}
