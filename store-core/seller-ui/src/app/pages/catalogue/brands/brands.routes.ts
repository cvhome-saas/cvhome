import {Routes} from '@angular/router';
import {BrandsComponent} from './brands.component';
import {BrandCreationComponent} from './brand-creation/brand-creation.component';
import {BrandDetailsComponent} from './brand-details/brand-details.component';
import {BrandsListComponent} from './brands-list/brands-list.component';

export const BRANDS_ROUTES: Routes = [
  {
    path: '',
    component: BrandsComponent,
    children: [
      {
        path: 'create-brand',
        component: BrandCreationComponent,
      },
      {
        path: 'brands-list',
        component: BrandsListComponent,
      },
      {
        path: 'brand/:id',
        component: BrandDetailsComponent,
      },
    ],
  }
];
