import {Routes} from '@angular/router';
import {OrgManagementComponent} from './org-management.component';
import {OrgListComponent} from './org-list/org-list.component';
import {CreateNewOrgComponent} from './create-new-org/create-new-org.component';
import {UpdateOrgDetailsComponent} from './update-org-details/update-org-details.component';
import {OrgChangePasswordComponent} from './org-change-password/org-change-password.component';
import {OrgStoresListComponent} from './org-stores-list/org-stores-list.component';

export const ORG_MANAGEMENT_ROUTES: Routes = [
  {
    path: '', component: OrgManagementComponent, children: [
      {
        path: 'create-org',
        component: CreateNewOrgComponent,
      },
      {
        path: 'org-list',
        component: OrgListComponent,
      },
      {
        path: 'org/:id',
        component: UpdateOrgDetailsComponent,
      },
      {
        path: 'org/:id/change-password',
        component: OrgChangePasswordComponent,
      },
      {
        path: 'org/:id/stores',
        component: OrgStoresListComponent,
      }
    ],
  }
];
