import {Injectable, inject} from '@angular/core';
import {ProductGroupsService} from '../services/product-groups.service';
import {ErrorService} from '../../../shared/services/error.service';
import {ReadableProductGroup} from '../models/product-group.model';

@Injectable()
export class ActiveButtonFacade {
  private readonly productGroupsService = inject(ProductGroupsService);
  private readonly errorService = inject(ErrorService);

  toggleActive(rowData: ReadableProductGroup, currentValue: boolean): boolean {
    const newValue = !currentValue;
    const group = {
      active: newValue,
      code: rowData?.code
    };
    this.productGroupsService.updateGroupActiveValue(group).subscribe({
      next: () => {
        this.errorService.success('PRODUCT_GROUP.GROUP_ACTIVATION');
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
    return newValue;
  }
}
