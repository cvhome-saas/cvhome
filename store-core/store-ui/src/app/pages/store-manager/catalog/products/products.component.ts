import {Component} from '@angular/core';
import {ManagerStoreId} from "../../../../shared/domain/commons";

@Component({
  selector: 'ngx-products',
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.scss']
})
export class ProductsComponent {
  storeSelectedChanged($event: ManagerStoreId) {
  }
}
