import {Routes} from '@angular/router';
import {StoreManagementComponent} from './store-management.component';
import {StoreCreationComponent} from './store-creation/store-creation.component';
import {StoresListComponent} from './stores-list/stores-list.component';
import {StoreLandingPageComponent} from './store-landing-page/store-landing-page.component';
import {StoreDetailInfoComponent} from './store-detail-info/store-detail-info.component';
import {StoreBrandingComponent} from './store-branding/store-branding.component';
import {StoreDomainComponent} from './store-domain/store-domain.component';
import {StoreSocialLinksComponent} from './store-social-links/store-social-links.component';
import {StoreSliderImagesComponent} from './store-slider-images/store-slider-images.component';
import {StoreSocialLoginComponent} from './store-social-login/store-social-login.component';
import {StorePaymentConfigurationComponent} from './store-payment-configuration/store-payment-configuration.component';

export const STORE_MANAGEMENT_ROUTES: Routes = [
  {
    path: '', component: StoreManagementComponent, children: [
      {
        path: 'create-store',
        component: StoreCreationComponent,
      },
      {
        path: 'stores-list',
        component: StoresListComponent,
      },
      {
        path: 'store-landing/:code',
        component: StoreLandingPageComponent,
      },
      {
        path: 'store/:code',
        component: StoreDetailInfoComponent,
      },
      {
        path: 'store-branding/:code',
        component: StoreBrandingComponent,
      },
      {
        path: 'store-domain/:code',
        component: StoreDomainComponent,
      },
      {
        path: 'store-social-links/:code',
        component: StoreSocialLinksComponent,
      },
      {
        path: 'store-slider-images/:code',
        component: StoreSliderImagesComponent,
      },
      {
        path: 'store-social-login/:code',
        component: StoreSocialLoginComponent,
      },
      {
        path: 'store-payment-configuration/:code',
        component: StorePaymentConfigurationComponent,
      }
    ],
  }
];
