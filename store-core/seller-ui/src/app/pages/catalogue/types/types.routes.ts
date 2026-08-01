import {Routes} from '@angular/router';
import {TypeDetailsComponent} from './type-details/type-details.component';
import {TypesListComponent} from './types-list/types-list.component';
import {TypesComponent} from './types.component';

export const TYPES_ROUTES: Routes = [
  {
    path: '',
    component: TypesComponent,
    children: [
      {
        path: 'create-type',
        component: TypeDetailsComponent,
      },
      {
        path: 'types-list',
        component: TypesListComponent,
      },
      {
        path: 'type/:id',
        component: TypeDetailsComponent,
      },
    ],
  }
];
