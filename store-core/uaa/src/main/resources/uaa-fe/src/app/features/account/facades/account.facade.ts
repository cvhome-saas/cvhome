import {Injectable, computed, inject, signal} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {TranslocoService} from '@jsverse/transloco';

import {ApiErrorService, AuthService, snapshot} from '@cvhome-saas/ui-kit';
import {passwordsMatch} from '@cvhome-saas/ui-kit/forms';
import {ToastService} from '@cvhome-saas/ui-kit/ui';
import {AccountService, type SessionSummary} from '@cvhome-saas/ui-kit/uaa';

/**
 * The signed-in person's own account: who they are, their sessions, and a password change.
 *
 * A password change ends every other session and every token — said on the page before the button, because
 * a person who changes a password after a scare wants exactly that and one who changes it on a whim does not
 * expect it.
 */
@Injectable()
export class AccountFacade {
  private readonly account = inject(AccountService);
  private readonly auth = inject(AuthService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  readonly busy = signal(false);
  readonly me = computed(() => this.auth.getCachedAuthUser());

  readonly form = new FormGroup(
    {
      currentPassword: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
      password: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.minLength(8)]}),
      repeatPassword: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    },
    {validators: [passwordsMatch]},
  );

  private readonly sessionList = snapshot(
    () => ({}),
    () => this.account.sessions(),
  );

  readonly sessions = computed<readonly SessionSummary[]>(() => this.sessionList.value() ?? []);
  readonly sessionsLoading = this.sessionList.isLoading;
  readonly sessionsError = this.sessionList.error;
  readonly reloadSessions = () => this.sessionList.reload();
  readonly others = computed(() => this.sessions().filter((s) => !s.current).length);

  changePassword(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.busy.set(true);
    this.account.changePassword({currentPassword: value.currentPassword, newPassword: value.password}).subscribe({
      next: () => {
        this.busy.set(false);
        this.form.reset();
        this.toast.success(this.transloco.translate('account.toast.passwordChanged'));
        this.sessionList.reload();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.applyToForm(failure, this.form);
        this.apiErrors.notify(failure);
      },
    });
  }

  revoke(session: SessionSummary): void {
    this.account.revokeSession(session.id).subscribe({
      next: () => this.sessionList.reload(),
      error: (failure: unknown) => this.apiErrors.notify(failure),
    });
  }

  revokeOthers(): void {
    this.account.revokeOtherSessions().subscribe({
      next: ({revoked}) => {
        this.toast.success(this.transloco.translate('account.toast.signedOutOthers', {count: revoked}));
        this.sessionList.reload();
      },
      error: (failure: unknown) => this.apiErrors.notify(failure),
    });
  }
}
