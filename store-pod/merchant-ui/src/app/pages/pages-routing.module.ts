import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';

import {PagesComponent} from './pages.component';
import {canAccessSecuredPages} from "../shared/services/auth-guard.service";
import {NotFoundComponent} from "./not-found/not-found.component";

interface RemoteRoute {
  path: string;
  module: string;
}

const routes: Routes = [{
  path: '',
  canActivate: [canAccessSecuredPages],
  component: PagesComponent,
  children: [
    {
      path: 'orders',
      loadChildren: () => import('./orders/orders.module')
        .then(m => m.OrdersModule),
    }, {
      path: 'catalogue',
      loadChildren: () => import('./catalogue/catalogue.module')
        .then(m => m.CatalogueModule),

    }, {
      path: 'content',
      loadChildren: () => import('./content/content.module')
        .then(m => m.ContentModule),
    }, {
      path: 'customer',
      loadChildren: () => import('./customer/customer.module')
        .then(m => m.CustomersModule),
    }, {
      path: '**',
      component: NotFoundComponent
    }
  ],
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PagesRoutingModule {
}
