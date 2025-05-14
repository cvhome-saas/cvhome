import {Component, Input} from '@angular/core';

import {CategoryService} from '../services/category.service';
import {TranslateService} from '@ngx-translate/core';
import {NbToastrService} from "@nebular/theme";
import {ErrorService} from "../../../../shared/services/error.service";

@Component({
  selector: 'ngx-categories-visibility',
  standalone: false,

  template: `
    <nb-checkbox [checked]="value" (checkedChange)="clicked()"/>`,
})
export class CategoriesVisibilityComponent {
  @Input() value: boolean;
  @Input() store: string;
  @Input() rowData: any;

  constructor(
    private categoryService: CategoryService,
    private translate: TranslateService,
    private toastr: NbToastrService,
    private errorService: ErrorService
  ) {
  }

  clicked() {
    this.rowData.visible = !this.value;
    this.categoryService.updateCategoryVisibility(this.rowData, this.store)
      .subscribe(res => {
        this.toastr.success(this.translate.instant('CATEGORY.CATEGORY_VISIBILITY'));
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }

}
