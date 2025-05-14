import {Component, Input} from '@angular/core';

import {ProductGroupsService} from '../services/product-groups.service';
import {TranslateService} from "@ngx-translate/core";
import {NbToastrService} from "@nebular/theme";
import {ErrorService} from "../../../../shared/services/error.service";


@Component({
  selector: 'ngx-product-groups-active',
  standalone: false,
  template: `<nb-checkbox [checked]="value" (checkedChange)="clicked()"/>`,
})
export class ActiveButtonComponent {
  @Input() value: boolean;
  @Input() rowData: any;
  @Input() store: string;

  constructor(
    private productGroupsService: ProductGroupsService,
    private translate: TranslateService,
    private errorService: ErrorService,
    private toastr: NbToastrService) {
  }

  clicked() {
    this.value = !this.value;
    const group = {
      active: this.value,
      code: this.rowData.code,
    };
    this.productGroupsService.updateGroupActiveValue(this.store, group)
      .subscribe(res => {
        this.toastr.success(this.translate.instant('PRODUCT_GROUP.GROUP_ACTIVATION'));
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }

}
