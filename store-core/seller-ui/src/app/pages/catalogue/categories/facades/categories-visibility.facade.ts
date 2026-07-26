import {Injectable, inject} from '@angular/core';
import {CategoryService} from '../services/category.service';
import {TranslateService} from '@ngx-translate/core';
import {NbToastrService} from '@nebular/theme';
import {ErrorService} from '../../../shared/services/error.service';

@Injectable()
export class CategoriesVisibilityFacade {
  private readonly categoryService = inject(CategoryService);
  private readonly translate = inject(TranslateService);
  private readonly toastr = inject(NbToastrService);
  private readonly errorService = inject(ErrorService);

  toggleVisibility(rowData: any, value: boolean): void {
    if (!rowData) return;
    rowData.visible = !value;
    this.categoryService.updateCategoryVisibility(rowData).subscribe({
      next: () => {
        this.toastr.success(this.translate.instant('CATEGORY.CATEGORY_VISIBILITY'));
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }
}
