import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ClientsComponent} from './clients.component';
import {ClientsListComponent} from './clients-list-component/clients-list-component';
import {ClientCreateComponent} from './client-create-component/client-create-component';
import {ClientEditComponent} from './client-edit-component/client-edit-component';

const routes: Routes = [
  {
    path: '',
    component: ClientsComponent,
    children: [
      {
        path: '',
        component: ClientsListComponent
      },
      {
        path: 'create',
        component: ClientCreateComponent
      },
      {
        path: 'edit/:clientId',
        component: ClientEditComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ClientsRoutingModule {
}
