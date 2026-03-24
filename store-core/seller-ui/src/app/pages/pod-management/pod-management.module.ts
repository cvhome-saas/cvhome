import {NgModule} from '@angular/core';
import {PodManagementRoutingModule} from './pod-management-routing.module';
import {NbCardModule, NbIconModule, NbInputModule, NbButtonModule, NbSpinnerModule} from '@nebular/theme';
import {SharedModule} from "../shared/shared.module";
import {PodListComponent} from "./pod-list/pod-list.component";
import {PodManagementComponent} from "./pod-management.component";
import {CreatePodComponent} from "./create-pod/create-pod.component";
import {EditPodComponent} from "./edit-pod/edit-pod.component";
import {PodFormComponent} from "./pod-form/pod-form.component";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";

@NgModule({
  declarations: [
    PodListComponent,
    CreatePodComponent,
    EditPodComponent,
    PodFormComponent,
    PodManagementComponent
  ],
  imports: [
    PodManagementRoutingModule,
    SharedModule,
    NbCardModule,
    NbIconModule,
    NbInputModule,
    NbButtonModule,
    NgxDatatableModule,
    NbSpinnerModule
  ]
})
export class PodManagementModule {
}
