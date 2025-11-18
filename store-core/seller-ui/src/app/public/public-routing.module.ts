import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {PublicComponent} from "./public.component";
import {IndexComponent} from "./index/index.component";
import {SignUpComponent} from "./sign-up/sign-up.component";
import {TermsComponent} from "./terms/terms.component";
import {PrivacyPolicyComponent} from "./privacy-policy/privacy-policy.component";

let routes: Routes = [
    {
        path: '',
        component: PublicComponent,
        children: [
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
          },
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule],
})
export class PublicRoutingModule {

}
