import {NgModule} from '@angular/core';


import {ProductsComponent} from './products.component';
import {ProductsListComponent} from './products-list/products-list.component';
import {ProductCreationComponent} from './product-creation/product-creation.component';
import {ProductFormComponent} from './product-form/product-form.component';
import {ProductToCategoryComponent} from './product-to-category/product-to-category.component';
import {ProductsRoutingModule} from './routing/products-routing.module';
import {ProductsImagesComponent} from './products-images/products-images.component';
import {NbDialogModule} from '@nebular/theme';
import {SharedModule} from "../../../shared/shared.module";
import {ProductDetailsComponent} from "./product-details/product-details.component";
import {ProductRelatedComponent} from "./product-related/product-related.component";

@NgModule({

  declarations: [
    ProductsComponent,
    ProductsListComponent,
    ProductCreationComponent,
    ProductFormComponent,
    ProductDetailsComponent,
    ProductToCategoryComponent,
    ProductRelatedComponent,
    ProductsImagesComponent,
  ],
  imports: [
    ProductsRoutingModule,
    SharedModule,
    NbDialogModule.forChild(),

  ],
})

export class ProductsModule {
}
