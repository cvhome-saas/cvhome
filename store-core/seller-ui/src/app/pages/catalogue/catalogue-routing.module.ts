import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {CatalogueComponent} from './catalogue.component';

const routes: Routes = [{
  path: '',
  component: CatalogueComponent,
  children: [
    {
      path: 'categories',
      loadChildren: () => import('./categories/categories.module')
        .then(m => m.CategoriesModule),
    },
    {
      path: 'products',
      loadChildren: () => import('./products/products.module')
        .then(m => m.ProductsModule),
    },
    {
      path: 'brands',
      loadChildren: () => import('./brands/brands.module')
        .then(m => m.BrandsModule),
    },
    {
      path: 'products-groups',
      loadChildren: () => import('./products-groups/products-groups.module')
        .then(m => m.ProductsGroupsModule),
    },
    {
      path: 'types',
      loadChildren: () => import('./types/types.module')
        .then(m => m.TypesModule),
    },
  ]
}];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CatalogueRoutingModule {
}
