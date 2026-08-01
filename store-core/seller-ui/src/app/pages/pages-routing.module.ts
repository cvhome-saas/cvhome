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
      loadChildren: () => import('./home/home.routes')
        .then(m => m.HOME_ROUTES)
    }, {
      path: 'user-management',
      loadChildren: () => import('./user-management/user-management.routes')
        .then(m => m.USER_MANAGEMENT_ROUTES),
    }, {
      path: 'org-management',
      loadChildren: () => import('./org-management/org-management.routes')
        .then(m => m.ORG_MANAGEMENT_ROUTES),
    }, {
      path: 'store-management',
      loadChildren: () => import('./store-management/store-management.routes')
        .then(m => m.STORE_MANAGEMENT_ROUTES),
    }, {
      path: 'pod-management',
      loadChildren: () => import('./pod-management/pod-management.routes')
        .then(m => m.POD_MANAGEMENT_ROUTES),
    }, {
      path: 'subscription-and-usage',
      loadChildren: () => import('./subscription-and-usage/subscription-and-usage.routes')
        .then(m => m.SUBSCRIPTION_AND_USAGE_ROUTES),
    },
    {
      path: 'orders',
      loadChildren: () => import('./orders/orders.routes')
        .then(m => m.ORDERS_ROUTES),
    }, {
      path: 'payment',
      loadChildren: () => import('./payment/payment.routes')
        .then(m => m.PAYMENT_ROUTES),
    }, {
      path: 'catalogue',
      loadChildren: () => import('./catalogue/catalogue.routes')
        .then(m => m.CATALOGUE_ROUTES),

    }, {
      path: 'content',
      loadChildren: () => import('./content/content.routes')
        .then(m => m.CONTENT_ROUTES),
    }, {
      path: 'customer',
      loadChildren: () => import('./customer/customer.routes')
        .then(m => m.CUSTOMER_ROUTES),
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
