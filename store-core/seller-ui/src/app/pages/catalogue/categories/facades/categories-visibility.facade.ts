import {Injectable, inject} from '@angular/core';
import {CategoryService} from '../services/category.service';
import {ApiErrorService} from '../../../../core/errors/api-error.service';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {PersistableCategory} from '../models/category.model';

@Injectable()
export class CategoriesVisibilityFacade {
  private readonly categoryService = inject(CategoryService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);

  toggleVisibility(rowData: PersistableCategory, value: boolean): void {
    if (!rowData) return;
    rowData.visible = !value;
    this.categoryService.updateCategoryVisibility(rowData).subscribe({
      next: () => {
        this.notify.success('CATEGORY.CATEGORY_VISIBILITY');
      },
      error: (err) => this.apiErrors.notify(err)
    });
  }
}
