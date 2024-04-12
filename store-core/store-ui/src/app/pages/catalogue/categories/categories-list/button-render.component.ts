import { Component, Input } from '@angular/core';

import { CategoryService } from '../services/category.service';
import { TranslateService } from '@ngx-translate/core';
import {NbToastrService} from "@nebular/theme";

@Component({
  selector: 'ngx-categories-visibility',

  template: `
    <input type="checkbox" [checked]="value" (click)="clicked()"/>
  `,
})
export class ButtonRenderComponent {
  @Input() value: boolean;
  @Input() store: string ;
  @Input() rowData: any;

  constructor(
    private categoryService: CategoryService,
    private translate: TranslateService,
    private toastr: NbToastrService,
  ) {
  }

  clicked() {
    this.rowData.visible = !this.value;
    this.categoryService.updateCategoryVisibility(this.rowData,this.store)
      .subscribe(res => {
        this.toastr.success(this.translate.instant('CATEGORY.CATEGORY_VISIBILITY'));
      });
  }

}
