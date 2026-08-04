import {Routes} from '@angular/router';
import {PublicComponent} from './public.component';
import {IndexComponent} from './index/index.component';
import {SignUpComponent} from './sign-up/sign-up.component';
import {TermsComponent} from './terms/terms.component';
import {PrivacyPolicyComponent} from './privacy-policy/privacy-policy.component';
import {ExternalLogoutLinkComponent} from './routing/external-logout-link.component';

export const PUBLIC_ROUTES: Routes = [
  {
    path: '',
    component: PublicComponent,
    children: [
      {
        path: '', component: IndexComponent
      },
      {
        path: 'signup', component: SignUpComponent
      },
      {
        path: 'terms', component: TermsComponent
      },
      {
        path: 'privacy-policy', component: PrivacyPolicyComponent
      },
      {
        path: 'external-logout-link',
        component: ExternalLogoutLinkComponent,
      },
      {
        path: 'subscription',
        loadChildren: () => import('./subscription/subscription.routes')
          .then(m => m.SUBSCRIPTION_ROUTES)
      },
    ]
  }
];
