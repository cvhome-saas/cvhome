import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';

import {PagesComponent} from './pages.component';
import {canAccessSecuredPages} from "./shared/services/auth-guard.service";
import {NotFoundComponent} from "./not-found/not-found.component";


const routes: Routes = [{
  path: '',
  canActivate: [canAccessSecuredPages],
  component: PagesComponent,
  children: [
    {
      path: '',
      loadChildren: () => import('./home/home.module')
        .then(m => m.HomeModule)
    }, {
      path: 'user-management',
      loadChildren: () => import('./user-management/user-management.module')
        .then(m => m.UserManagementModule),
    }, {
      path: 'org-management',
      loadChildren: () => import('./org-management/org-management.module')
        .then(m => m.OrgManagementModule),
    }, {
      path: 'store-management',
      loadChildren: () => import('./store-management/store-management.module')
        .then(m => m.StoreManagementModule),
    }, {
      path: 'pod-management',
      loadChildren: () => import('./pod-management/pod-management.module')
        .then(m => m.PodManagementModule),
    }, {
      path: 'subscription-and-usage',
      loadChildren: () => import('./subscription-and-usage/subscription-and-usage.module')
        .then(m => m.SubscriptionAndUsageModule),
    },
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
    },
    {
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
