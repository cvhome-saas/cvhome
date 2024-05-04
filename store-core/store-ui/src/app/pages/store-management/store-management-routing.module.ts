import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {StoreManagementComponent} from './store-management.component';
import {StoreDetailsComponent} from './store-details/store-details.component';
import {StoreCreationComponent} from './store-creation/store-creation.component';
import {StoresListComponent} from './stores-list/stores-list.component';
import {StoreLandingPageComponent} from './store-landing-page/store-landing-page.component';
import {StoreDetailInfoComponent} from './store-detail-info/store-detail-info.component';
import {StoreBrandingComponent} from './store-branding/store-branding.component';

const routes: Routes = [
  {
    path: '', component: StoreManagementComponent, children: [
      {
        path: 'store',
        component: StoreDetailsComponent,
        // canActivate: [SuperuserAdminRetailStoreGuard]
      },
      {
        path: 'create-store',
        component: StoreCreationComponent,
        // canActivate: [SuperuserAdminRetailGuard]
      },
      {
        path: 'stores-list',
        component: StoresListComponent,
        // canActivate: [SuperuserAdminGuard]
      },
      {
        path: 'store-landing/:code',
        component: StoreLandingPageComponent,
        // canActivate: [SuperuserAdminRetailStoreGuard]
      },
      {
        path: 'store/:code',
        component: StoreDetailInfoComponent,
        // canActivate: [SuperuserAdminRetailStoreGuard]
      },
      {
        path: 'store-branding/:code',
        component: StoreBrandingComponent,
        // canActivate: [SuperuserAdminRetailStoreGuard]
      }
    ],
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StoreManagementRoutingModule {
}
