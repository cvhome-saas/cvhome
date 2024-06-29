import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';

import {PagesComponent} from './pages.component';
import {canAccessSecuredPages} from "../shared/service/auth-guard.service";
import {NotFoundComponent} from "./not-found/not-found.component";
// import {DashboardComponent} from "./dashboard/dashboard.component";
// import { DashboardComponent } from './dashboard/dashboard.component';
// import { ECommerceComponent } from './e-commerce/e-commerce.component';
// import { NotFoundComponent } from './miscellaneous/not-found/not-found.component';

const routes: Routes = [{
  path: '',
  canActivate: [canAccessSecuredPages],
  component: PagesComponent,
  children: [
    {
      path: 'home',
      loadChildren: () => import('./home/home.module')
        .then(m => m.HomeModule)
    },
    {
      path: 'orders',
      loadChildren: () => import('./orders/orders.module')
        .then(m => m.OrdersModule),
    }, {
      path: 'user-management',
      loadChildren: () => import('./user-management/user-management.module')
        .then(m => m.UserManagementModule),
    }, {
      path: 'store-management',
      loadChildren: () => import('./store-management/store-management.module')
        .then(m => m.StoreManagementModule),
    }, {
      path: 'catalogue',
      // canActivate: [SuperadminStoreRetailCatalogueGuard],
      loadChildren: () => import('./catalogue/catalogue.module')
        .then(m => m.CatalogueModule),

    }, {
      path: 'content',
      loadChildren: () => import('./content/content.module')
        .then(m => m.ContentModule),
    }, {
      path: 'customer',
      loadChildren: () => import('./customers/customer.module')
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
