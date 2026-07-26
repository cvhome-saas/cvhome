import {Injectable, inject} from '@angular/core';
import {UserService} from '../../../shared/services/user.service';
import {TranslateService} from '@ngx-translate/core';
import {NbToastrService} from '@nebular/theme';
import {ErrorService} from '../../../shared/services/error.service';

@Injectable()
export class UserStatusFacade {
  private readonly userService = inject(UserService);
  private readonly translate = inject(TranslateService);
  private readonly toastr = inject(NbToastrService);
  private readonly errorService = inject(ErrorService);

  toggleStatus(rowData: any, store: string): void {
    if (rowData.active) {
      this.userService.disable(rowData.id, store).subscribe({
        next: () => {
          this.toastr.success(this.translate.instant('USER_FORM.USER_DISABLED'));
          rowData.active = false;
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
    } else {
      this.userService.enable(rowData.id, store).subscribe({
        next: () => {
          this.toastr.success(this.translate.instant('USER_FORM.USER_ENABLED'));
          rowData.active = true;
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
    }
  }
}
