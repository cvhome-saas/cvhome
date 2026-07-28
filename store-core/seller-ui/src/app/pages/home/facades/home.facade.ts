import {DestroyRef, Injectable, computed, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AuthService, UserType} from '../../shared/services/auth.service';

@Injectable()
export class HomeFacade {
  private readonly authService = inject(AuthService);

  private readonly userType = signal<UserType | null>(null);

  readonly isClientUser = computed(() => {
    const type = this.userType();
    return type === UserType.ORG_USER || type === UserType.MANAGED_USER;
  });

  readonly isAdminUser = computed(() => this.userType() === UserType.SUPPER_USER);

  init(destroyRef: DestroyRef): void {
    this.authService.getAuthUser()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe((user) => this.userType.set(user.user_type));
  }
}
