import {NgModule} from '@angular/core';
import {OrgManagementRoutingModule} from './org-management-routing.module';
import {NbDialogModule} from '@nebular/theme';
import {SharedModule} from "../shared/shared.module";
import {NgxFileDropModule} from "ngx-file-drop";
import {OrgListComponent} from "./org-list/org-list.component";
import {OrgManagementComponent} from "./org-management.component";
import {UpdateOrgDetailsComponent} from "./update-org-details/update-org-details.component";
import {CreateNewOrgComponent} from "./create-new-org/create-new-org.component";
import {OrgChangePasswordComponent} from "./org-change-password/org-change-password.component";
import {OrgStoresListComponent} from "./org-stores-list/org-stores-list.component";

@NgModule({
  declarations: [
    OrgListComponent,
    UpdateOrgDetailsComponent,
    CreateNewOrgComponent,
    OrgManagementComponent,
    OrgChangePasswordComponent,
    OrgStoresListComponent
  ],
  imports: [
    OrgManagementRoutingModule,
    SharedModule,
    NbDialogModule.forChild(),
    NgxFileDropModule,
  ]
})
export class OrgManagementModule {
}
