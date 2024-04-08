import {Component} from '@angular/core';
import { ManagerStoreId} from "../../../../shared/domain/commons";

@Component({
  selector: 'ngx-category',
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.scss']
})
export class CategoryComponent {
  rows = [
    { name: 'Austin', gender: 'Male', company: 'Swimlane' },
    { name: 'Dany', gender: 'Male', company: 'KFC' },
    { name: 'Molly', gender: 'Female', company: 'Burger King' }
  ];
  columns = [{ name: 'name' }, { name: 'Gender' }, { name: 'Company' }];
  storeSelectedChanged($event: ManagerStoreId) {
  }
}
