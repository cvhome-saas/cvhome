import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {PublicComponent} from "./public.component";

let routes: Routes = [
  {
    path: '',
    component: PublicComponent,
    children: [
      {
        path: 'subscription',
        loadChildren: () => import('./subscription/subscription.module')
          .then(m => m.SubscriptionModule)
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
