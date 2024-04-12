import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';

import {PagesComponent} from './pages.component';
import {NotFoundComponent} from "./learn/miscellaneous/not-found/not-found.component";
import {canActivateTeam, canActivateTeamu} from "../shared/service/auth-guard.service";
// import {DashboardComponent} from "./dashboard/dashboard.component";
// import { DashboardComponent } from './dashboard/dashboard.component';
// import { ECommerceComponent } from './e-commerce/e-commerce.component';
// import { NotFoundComponent } from './miscellaneous/not-found/not-found.component';

const routes: Routes = [{
  path: '',
  canActivate: [canActivateTeam],
  canActivateChild: [canActivateTeamu],
  component: PagesComponent,
  children: [
    // {
    //   path: 'home',
    //   loadChildren: 'app/pages/home/home.module#HomeModule'
    // },
    {
      path: 'orders',
      loadChildren: () => import('./orders/orders.module')
        .then(m => m.OrdersModule),
    },
    {
      path: 'user-management',
      loadChildren: () => import('./user-management/user-management.module')
        .then(m => m.UserManagementModule),
    },
    // {
    //   path: 'store-management',
    //   loadChildren: 'app/pages/store-management/store-management.module#StoreManagementModule'
    // },
    // {
    //   path: 'catalogue',
    //   canActivate: [SuperadminStoreRetailCatalogueGuard],
    //   loadChildren: 'app/pages/catalogue/catalogue.module#CatalogueModule'
    // },
    {
      path: 'content',
      loadChildren: () => import('./content/content.module')
        .then(m => m.ContentModule),
    },
    // {
    //   path: 'shipping',
    //   loadChildren: 'app/pages/shipping/shipping.module#ShippingModule'
    // },
    // {
    //   path: 'payment',
    //   loadChildren: 'app/pages/payment/payment.module#PaymentModule'
    // },
    // {
    //   path: 'tax-management',
    //   loadChildren: 'app/pages/tax-management/tax-management.module#TaxManagementModule'
    // },
     {
       path: 'customer',
       loadChildren: () => import('./customers/customer.module')
         .then(m => m.CustomersModule),
     },
    // {
    //   path: 'error-500',
    //   component: FiveHundredComponent
    // },
    // {
    //   path: '',
    //   redirectTo: 'home',
    //   pathMatch: 'full'
    // },
    // {
    //   path: '**',
    //   component: NotFoundComponent
    // }


    // {
    //   path: 'dashboard',
    //   component: ECommerceComponent,
    // },
    // {
    //   path: 'iot-dashboard',
    //   component: DashboardComponent,
    // },
    {
      path: 'store-manager',
      loadChildren: () => import('./store-manager/store-manager.module')
        .then(m => m.StoreManagerModule),
    },
    {
      path: 'layout',
      loadChildren: () => import('./learn/layout/layout.module')
        .then(m => m.LayoutModule),
    },

    {
      path: 'forms',
      loadChildren: () => import('./learn/forms/forms.module')
        .then(m => m.FormsModule),
    },
    {
      path: 'ui-features',
      loadChildren: () => import('./learn/ui-features/ui-features.module')
        .then(m => m.UiFeaturesModule),
    },
    {
      path: 'modal-overlays',
      loadChildren: () => import('./learn/modal-overlays/modal-overlays.module')
        .then(m => m.ModalOverlaysModule),
    },
    {
      path: 'extra-components',
      loadChildren: () => import('./learn/extra-components/extra-components.module')
        .then(m => m.ExtraComponentsModule),
    },
    // {
    //   path: 'maps',
    //   loadChildren: () => import('./maps/maps.module')
    //     .then(m => m.MapsModule),
    // },
    // {
    //   path: 'charts',
    //   loadChildren: () => import('./charts/charts.module')
    //     .then(m => m.ChartsModule),
    // },
    {
      path: 'editors',
      loadChildren: () => import('./learn/editors/editors.module')
        .then(m => m.EditorsModule),
    },
    {
      path: 'miscellaneous',
      loadChildren: () => import('./learn/miscellaneous/miscellaneous.module')
        .then(m => m.MiscellaneousModule),
    },
    {
      path: '',
      redirectTo: 'dashboard',
      pathMatch: 'full',
    },
    {
      path: '**',
      component: NotFoundComponent,
    },
  ],
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PagesRoutingModule {
}
