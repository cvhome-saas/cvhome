import {Routes} from '@angular/router';
import {IndexComponent} from "./pages/index/index.component";
import {SignUpComponent} from "./pages/sign-up/sign-up.component";
import {TermsComponent} from "./pages/terms/terms.component";
import {PrivacyPolicyComponent} from "./pages/privacy-policy/privacy-policy.component";

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
