import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {PodManagementComponent} from './pod-management.component';
import {PodListComponent} from './pod-list/pod-list.component';
import {CreatePodComponent} from './create-pod/create-pod.component';
import {EditPodComponent} from './edit-pod/edit-pod.component';

const routes: Routes = [{
  path: '',
  component: PodManagementComponent,
  children: [
    {
      path: 'list',
      component: PodListComponent,
    },
    {
      path: 'create-pod',
      component: CreatePodComponent,
    },
     {
      path: 'pod/:id',
      component: EditPodComponent,
    },
    {
      path: '',
      redirectTo: 'list',
      pathMatch: 'full',
    },
  ],
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PodManagementRoutingModule {
}
