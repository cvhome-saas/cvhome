import {Injectable, computed, inject, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {ApiErrorService, snapshot} from '@cvhome-saas/ui-kit';
import {ToastService, type RoleChange, type RoleOption} from '@cvhome-saas/ui-kit/ui';
import {AdminUserService, type AdminUserAction, type PlatformUserRow} from '@cvhome-saas/ui-kit/uaa';

import {UsersApi} from '../services/users.api.service';

export const PAGE_SIZE = 20;

/**
 * Every account uaa knows about, and what a super admin may do to one.
 *
 * **No organization filter, unlike console-ui's platform users page.** That console asks "who is in
 * this org"; this one is uaa's own administration and lists everyone. uaa's list endpoint matches
 * metadata equality and offers no text query either, so there is no search box to add — see the
 * notice the page renders.
 *
 * **`SuperAdminImmutableException` is not predicted.** uaa refuses to disable or delete the
 * platform's own super admin, and which account that is belongs to uaa's configuration. The action
 * is offered and the server's refusal is rendered, rather than a control hidden on a guess.
 */
@Injectable()
export class UsersFacade {
  private readonly api = inject(UsersApi);
  private readonly admin = inject(AdminUserService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  readonly busy = signal(false);
  readonly pageIndex = signal(0);

  /** The account a dialog is acting on, or null. */
  readonly resetting = signal<PlatformUserRow | null>(null);
  readonly editingRoles = signal<PlatformUserRow | null>(null);
  readonly deleting = signal<PlatformUserRow | null>(null);

  private readonly users = snapshot(
    () => ({page: this.pageIndex(), count: PAGE_SIZE}),
    (query) => this.api.load(query),
  );

  private readonly roles = snapshot(
    () => ({}),
    () => this.api.assignableRoles(),
  );

  readonly isLoading = this.users.isLoading;
  readonly error = this.users.error;
  readonly isEmpty = this.users.isEmpty;
  readonly reload = () => this.users.reload();

  readonly rows = computed(() => this.users.value()?.rows ?? []);
  readonly totalElements = computed(() => this.users.value()?.totalElements ?? 0);
  readonly totalPages = computed(() => this.users.value()?.totalPages ?? 0);

  readonly roleOptions = computed<readonly RoleOption[]>(() =>
    (this.roles.value() ?? []).map((role) => ({value: role, label: role})),
  );

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }

  dismissDialogs(): void {
    this.resetting.set(null);
    this.editingRoles.set(null);
    this.deleting.set(null);
  }

  confirmReset(password: string): void {
    const target = this.resetting();
    if (target) {
      this.apply(target, {kind: 'resetPassword', password}, 'users.toast.passwordReset');
    }
  }

  confirmRoles(change: RoleChange): void {
    const target = this.editingRoles();
    if (target) {
      this.apply(target, {kind: 'setRoles', ...change}, 'users.toast.rolesSaved');
    }
  }

  confirmDelete(): void {
    const target = this.deleting();
    if (target) {
      this.apply(target, {kind: 'delete'}, 'users.toast.deleted');
    }
  }

  toggleEnabled(row: PlatformUserRow): void {
    this.apply(
      row,
      {kind: row.enabled ? 'disable' : 'enable'},
      row.enabled ? 'users.toast.disabled' : 'users.toast.enabled',
    );
  }

  /**
   * One write, then a re-read.
   *
   * The list is re-fetched rather than patched in place because most of these endpoints answer
   * `void`: what the page then shows is what uaa stored, not what this console assumed.
   */
  private apply(row: PlatformUserRow, action: AdminUserAction, successKey: string): void {
    this.busy.set(true);
    this.admin.apply(row.id, action).subscribe({
      next: () => {
        this.busy.set(false);
        this.dismissDialogs();
        this.toast.success(this.transloco.translate(successKey, {name: row.username}));
        this.users.reload();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }
}
