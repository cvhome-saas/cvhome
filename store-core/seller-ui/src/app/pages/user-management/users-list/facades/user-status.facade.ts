import {Injectable, inject} from '@angular/core';
import {UserService} from '../../../shared/services/user.service';
import {ErrorService} from '../../../shared/services/error.service';

@Injectable()
export class UserStatusFacade {
  private readonly userService = inject(UserService);
  private readonly errorService = inject(ErrorService);

  toggleStatus(rowData: any, store: string): void {
    if (rowData.active) {
      this.userService.disable(rowData.id, store).subscribe({
        next: () => {
          this.errorService.success('USER_FORM.USER_DISABLED');
          rowData.active = false;
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
    } else {
      this.userService.enable(rowData.id, store).subscribe({
        next: () => {
          this.errorService.success('USER_FORM.USER_ENABLED');
          rowData.active = true;
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
    }
  }
}
