import {Injectable, inject} from '@angular/core';
import {CategoryService} from '../services/category.service';
import {ErrorService} from '../../../shared/services/error.service';

@Injectable()
export class CategoriesVisibilityFacade {
  private readonly categoryService = inject(CategoryService);
  private readonly errorService = inject(ErrorService);

  toggleVisibility(rowData: any, value: boolean): void {
    if (!rowData) return;
    rowData.visible = !value;
    this.categoryService.updateCategoryVisibility(rowData).subscribe({
      next: () => {
        this.errorService.success('CATEGORY.CATEGORY_VISIBILITY');
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }
}
