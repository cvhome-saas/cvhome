import {Routes} from '@angular/router';
import {IndexComponent} from "./public/index/index.component";
import {SignUpComponent} from "./public/sign-up/sign-up.component";
import {TermsComponent} from "./public/terms/terms.component";
import {PrivacyPolicyComponent} from "./public/privacy-policy/privacy-policy.component";

export const routes: Routes = [
  {
    path: "", component: IndexComponent
  },
  {
    path: "signup", component: SignUpComponent
  },
  {
    path: "terms", component: TermsComponent
  },
  {
    path: "privacy-policy", component: PrivacyPolicyComponent
  }
];
