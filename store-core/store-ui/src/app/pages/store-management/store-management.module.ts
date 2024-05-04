import {NgModule} from '@angular/core';

import {StoreManagementComponent} from './store-management.component';
import {StoreManagementRoutingModule} from './store-management-routing.module';
import {StoreDetailsComponent} from './store-details/store-details.component';
import {StoreCreationComponent} from './store-creation/store-creation.component';
import {StoresListComponent} from './stores-list/stores-list.component';
import {StoreFormComponent} from './store-form/store-form.component';
import {StoreLandingPageComponent} from './store-landing-page/store-landing-page.component';
import {StoreDetailInfoComponent} from './store-detail-info/store-detail-info.component';
import {StoreBrandingComponent} from './store-branding/store-branding.component';
import {NbDialogModule} from '@nebular/theme';
import {SharedModule} from "../store-manager/shared/shared.module";

@NgModule({
  declarations: [
    StoreManagementComponent,
    StoreDetailsComponent,
    StoreCreationComponent,
    StoresListComponent,
    StoreFormComponent,
    StoreBrandingComponent,
    StoreFormComponent,
    StoreLandingPageComponent,
    StoreDetailInfoComponent,
  ],
  imports: [
    StoreManagementRoutingModule,

    /**
     * stop using AGM, incompatible with Angular 11
     * look at @types/googlemaps@
     */
    /**
     AgmCoreModule.forRoot({
     apiKey: environment.googleApiKey,
     libraries: ['places'],
     language: 'en'
     }),
     **/

    SharedModule,
    NbDialogModule.forChild(),
  ]
})
export class StoreManagementModule {
}
