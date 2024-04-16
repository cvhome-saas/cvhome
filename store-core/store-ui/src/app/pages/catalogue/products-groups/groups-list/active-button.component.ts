import {Component, Input} from '@angular/core';

import {ProductGroupsService} from '../services/product-groups.service';


@Component({
  selector: 'ngx-product-groups-active',
  template: `<input type="checkbox" [checked]="value" (click)="clicked() "/>`,
})
export class ActiveButtonComponent {
  @Input() value: boolean;
  @Input() rowData: any;
  @Input() store: string;

  constructor(
    private productGroupsService: ProductGroupsService
  ) {
  }

  clicked() {
    this.value = !this.value;
    const group = {
      active: this.value,
      code: this.rowData.code,
    };
    this.productGroupsService.updateGroupActiveValue(this.store, group)
      .subscribe(res => {
      });
  }

}
