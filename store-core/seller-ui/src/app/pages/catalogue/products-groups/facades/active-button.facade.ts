import {Injectable, inject} from '@angular/core';
import {ProductGroupsService} from '../services/product-groups.service';
import {ApiErrorService} from '../../../../core/errors/api-error.service';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {ReadableProductGroup} from '../models/product-group.model';

@Injectable()
export class ActiveButtonFacade {
  private readonly productGroupsService = inject(ProductGroupsService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);

  toggleActive(rowData: ReadableProductGroup, currentValue: boolean): boolean {
    const newValue = !currentValue;
    const group = {
      active: newValue,
      code: rowData?.code
    };
    this.productGroupsService.updateGroupActiveValue(group).subscribe({
      next: () => {
        this.notify.success('PRODUCT_GROUP.GROUP_ACTIVATION');
      },
      error: (err) => this.apiErrors.notify(err)
    });
    return newValue;
  }
}
