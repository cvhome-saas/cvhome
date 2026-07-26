import {Injectable, inject} from '@angular/core';
import {ProductGroupsService} from '../services/product-groups.service';
import {TranslateService} from '@ngx-translate/core';
import {NbToastrService} from '@nebular/theme';
import {ErrorService} from '../../../shared/services/error.service';

@Injectable()
export class ActiveButtonFacade {
  private readonly productGroupsService = inject(ProductGroupsService);
  private readonly translate = inject(TranslateService);
  private readonly toastr = inject(NbToastrService);
  private readonly errorService = inject(ErrorService);

  toggleActive(rowData: any, currentValue: boolean): boolean {
    const newValue = !currentValue;
    const group = {
      active: newValue,
      code: rowData?.code
    };
    this.productGroupsService.updateGroupActiveValue(group).subscribe({
      next: () => {
        this.toastr.success(this.translate.instant('PRODUCT_GROUP.GROUP_ACTIVATION'));
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
    return newValue;
  }
}
