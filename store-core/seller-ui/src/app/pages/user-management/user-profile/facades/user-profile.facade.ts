import {Injectable, inject, signal} from '@angular/core';
import {UserService} from 'seller-core';
import {User} from 'seller-core';
import {ApiErrorService} from 'seller-core';

@Injectable()
export class UserProfileFacade {
  private readonly userService = inject(UserService);
  private readonly apiErrors = inject(ApiErrorService);

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
        this.apiErrors.notify(err);
      }
    });
  }
}
