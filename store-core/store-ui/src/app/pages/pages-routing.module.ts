import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';

import {PagesComponent} from './pages.component';
import {canAccessSecuredPages} from "../shared/services/auth-guard.service";
import {NotFoundComponent} from "./not-found/not-found.component";
import {loadRemoteModule} from '@angular-architects/module-federation';
import {SelectedStoreService} from "../shared/services/selected-store.service";

interface RemoteRoute {
  path: string;
  module: string;
}

const remoteRoutes: RemoteRoute[] = [
  {path: 'orders', module: "OrdersModule"},
  {path: 'catalogue', module: "CatalogueModule"},
  {path: 'content', module: "ContentModule"},
  {path: 'customer', module: "CustomersModule"},
];


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
      path: 'subscription-and-usage',
      loadChildren: () => import('./subscription-and-usage/subscription-and-usage.module')
        .then(m => m.SubscriptionAndUsageModule),
    },
    ...remoteRoutes.map(it => {
      return {
        path: it.path,
        loadChildren: () => {
          let store = localStorage.getItem(SelectedStoreService.STORE_ID_KEY);
          let url = `/store-pod-gateway-v2/${store}/merchant-ui/remoteEntry.js`;
          return loadRemoteModule({
            remoteEntry: url,
            type: 'module',
            exposedModule: `./${it.module}`
          })
            .then((m) => m[it.module])
            .catch(handleRemoteModuleFailure);
        },
      }
    }),
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

// async function routedLoadRemoteModule(options: any, module: string) {
//   const storeId = localStorage.getItem(SelectedStoreService.STORE_ID_KEY)
//   if (storeId) {
//     options = {
//       ...options,
//       remoteEntry: options.remoteEntry + "?store=" + storeId
//     }
//   }
//   return loadRemoteModule(options)
//     .then((m) => m[module])
//     .catch(handleRemoteModuleFailure)
// }

async function handleRemoteModuleFailure() {
  return import(
    '../remote-module-placeholder/remote-module-placeholder.module'
    ).then((m) => m.RemoteModulePlaceholderModule);
}

