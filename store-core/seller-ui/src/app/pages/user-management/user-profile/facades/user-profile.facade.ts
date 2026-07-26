import {Injectable, inject, signal} from '@angular/core';
import {UserService} from '../../../shared/services/user.service';
import {User} from '../../../shared/models/user';
import {ErrorService} from '../../../shared/services/error.service';

@Injectable()
export class UserProfileFacade {
  private readonly userService = inject(UserService);
  private readonly errorService = inject(ErrorService);

  readonly user = signal<User | null>(null);
  readonly loading = signal<boolean>(false);

  init(): void {
    this.loading.set(true);
    this.userService.getCurrentAccount().subscribe({
      next: (user) => {
        this.user.set(user);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }
}
