import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ClientsRoutingModule} from './clients-routing.module';
import {SharedModule} from '../../shared/shared.module';
import {ClientsListComponent} from './clients-list-component/clients-list-component';
import {ClientEditComponent} from './client-edit-component/client-edit-component';
import {ClientFormComponent} from './client-form-component/client-form-component';
import {ClientCreateComponent} from './client-create-component/client-create-component';


@NgModule({
  declarations: [
    ClientsListComponent,
    ClientFormComponent,
    ClientEditComponent,
    ClientCreateComponent,
    ClientFormComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    ClientsRoutingModule,
  ]
})
export class ClientsModule {
}
