import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {PublicComponent} from './public.component';
import {LoginComponent} from './login/login.component';
import {ExternalLogoutComponent} from "./external-logout/external-logout.component";

const routes: Routes = [
  {
    path: '',
    component: PublicComponent,
    children: [
      {path: 'login', component: LoginComponent},
      {path: 'external-logout-link', component: ExternalLogoutComponent}
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PublicRoutingModule {
}
