import {Injectable, computed, inject, signal} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';
import {TranslocoService} from '@jsverse/transloco';

import {ApiErrorService, snapshot} from '@cvhome-saas/ui-kit';
import {ToastService, type RoleOption} from '@cvhome-saas/ui-kit/ui';
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

  /**
   * The account the detail pane is showing, or null.
   *
   * Three dialogs used to carry this — roles, set-password and delete each held their own target.
   * The pane is one subject, so the roles editor and the profile fields moved into it; only the two
   * genuinely modal moments keep a dialog, and each keys off this selection.
   */
  readonly selected = signal<PlatformUserRow | null>(null);

  /** Still dialogs: handing over a password, and a delete that cannot be undone. */
  readonly resetting = signal<PlatformUserRow | null>(null);
  readonly deleting = signal<PlatformUserRow | null>(null);

  /** The profile fields the pane edits. uaa's update takes names, enabled and roles — not email. */
  readonly form = new FormGroup({
    firstName: new FormControl('', {nonNullable: true}),
    lastName: new FormControl('', {nonNullable: true}),
  });

  /** Roles are a set, edited in the pane; the diff is computed on save. */
  readonly draftRoles = signal<readonly string[]>([]);

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
    this.deleting.set(null);
  }

  /** Picking a row fills the pane from the row the list already has — no fetch needed. */
  select(row: PlatformUserRow): void {
    this.form.reset({
      firstName: firstNameOf(row),
      lastName: lastNameOf(row),
    });
    this.draftRoles.set(row.roles);
    this.selected.set(row);
  }

  clearSelection(): void {
    this.selected.set(null);
  }

  isSelected(row: PlatformUserRow): boolean {
    return this.selected()?.id === row.id;
  }

  toggleRole(role: string): void {
    const held = new Set(this.draftRoles());
    if (held.has(role)) {
      held.delete(role);
    } else {
      held.add(role);
    }
    this.draftRoles.set([...held]);
  }

  /**
   * Saves the pane: the profile fields and the role set, as two calls.
   *
   * uaa has no "these are the roles now" endpoint — granting and revoking are separate paths — so
   * the diff is computed here and `apply` runs both legs. Saving names and roles together is one
   * operator action even though it is two requests.
   */
  save(): void {
    const target = this.selected();
    if (!target) {
      return;
    }
    const held = new Set(target.roles);
    const draft = new Set(this.draftRoles());
    const add = [...draft].filter((role) => !held.has(role));
    const remove = [...held].filter((role) => !draft.has(role));
    const raw = this.form.getRawValue();

    this.busy.set(true);
    this.admin
      .update(target.id, {firstName: raw.firstName.trim() || null, lastName: raw.lastName.trim() || null})
      .subscribe({
        next: () => {
          if (add.length === 0 && remove.length === 0) {
            this.settled(target, 'users.toast.saved');
            return;
          }
          this.apply(target, {kind: 'setRoles', add, remove}, 'users.toast.saved');
        },
        error: (failure: unknown) => {
          this.busy.set(false);
          this.apiErrors.applyToForm(failure, this.form);
          this.apiErrors.notify(failure);
        },
      });
  }

  private settled(row: PlatformUserRow, key: string): void {
    this.busy.set(false);
    this.dismissDialogs();
    this.toast.success(this.transloco.translate(key, {name: row.username}));
    this.users.reload();
  }

  confirmReset(password: string): void {
    const target = this.resetting();
    if (target) {
      this.apply(target, {kind: 'resetPassword', password}, 'users.toast.passwordReset');
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

/** uaa stores given and family names separately; `PlatformUserRow` joins them for display. */
function firstNameOf(row: PlatformUserRow): string {
  return row.name.split(' ')[0] ?? '';
}

function lastNameOf(row: PlatformUserRow): string {
  return row.name.split(' ').slice(1).join(' ');
}
