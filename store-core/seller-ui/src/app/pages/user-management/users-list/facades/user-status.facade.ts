import {Injectable, inject} from '@angular/core';
import {UserService} from 'seller-core';
import {ApiErrorService} from 'seller-core';
import {NotificationService} from '../../../../core/notifications/notification.service';
import {User} from 'seller-core';

@Injectable()
export class UserStatusFacade {
  private readonly userService = inject(UserService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly notify = inject(NotificationService);

  toggleStatus(rowData: User, store: string): void {
    if (rowData.active) {
      this.userService.disable(rowData.id, store).subscribe({
        next: () => {
          this.notify.success('USER_FORM.USER_DISABLED');
          rowData.active = false;
        },
        error: (err) => {
          this.apiErrors.notify(err);
        }
      });
    } else {
      this.userService.enable(rowData.id, store).subscribe({
        next: () => {
          this.notify.success('USER_FORM.USER_ENABLED');
          rowData.active = true;
        },
        error: (err) => {
          this.apiErrors.notify(err);
        }
      });
    }
  }
}
