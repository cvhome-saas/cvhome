import {Routes} from '@angular/router';
import {CatalogueComponent} from './catalogue.component';

export const CATALOGUE_ROUTES: Routes = [{
  path: '',
  component: CatalogueComponent,
  children: [
    {
      path: 'categories',
      loadChildren: () => import('./categories/categories.routes')
        .then(m => m.CATEGORIES_ROUTES),
    },
    {
      path: 'products',
      loadChildren: () => import('./products/products.routes')
        .then(m => m.PRODUCTS_ROUTES),
    },
    {
      path: 'brands',
      loadChildren: () => import('./brands/brands.routes')
        .then(m => m.BRANDS_ROUTES),
    },
    {
      path: 'products-groups',
      loadChildren: () => import('./products-groups/products-groups.routes')
        .then(m => m.PRODUCTS_GROUPS_ROUTES),
    },
    {
      path: 'types',
      loadChildren: () => import('./types/types.routes')
        .then(m => m.TYPES_ROUTES),
    },
  ]
}];
