import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {ProductsComponent} from '../products.component';
import {ProductsListComponent} from '../products-list/products-list.component';
import {ProductCreationComponent} from '../product-creation/product-creation.component';
import {ProductDetailsComponent} from "../product-details/product-details.component";
import {ProductsImagesComponent} from "../products-images/products-images.component";
import {ProductToCategoryComponent} from "../product-to-category/product-to-category.component";
import {ProductRelatedComponent} from "../product-related/product-related.component";


const routes: Routes = [
  {
    path: '',
    component: ProductsComponent,
    children: [
      {
        path: 'create-product/:code',
        // canDeactivate: [ExitGuard],
        component: ProductCreationComponent,
      },
      {
        path: 'products-list',
        component: ProductsListComponent,
      },
      // {
      //   path: 'product-ordering',
      //   component: ProductOrderingComponent
      // },
      {
        path: 'product/:code',
        // canDeactivate: [ExitGuard],
        component: ProductDetailsComponent,

        children: [
          {
            path: '',
            redirectTo: 'default',
            pathMatch: 'full',
          },
          {
            path: 'default', //images by default
            component: ProductsImagesComponent,
          },
          {
            path: 'images', //images by default
            component: ProductsImagesComponent,
          },
          {
            path: 'category',
            component: ProductToCategoryComponent,
          },
          {
            path: 'related',
            component: ProductRelatedComponent,
          },
        ],
      },
    ],
  }
];

@NgModule({
  imports: [
    RouterModule.forChild(routes)
  ],
  exports: [RouterModule]
})

export class ProductsRoutingModule {
}
