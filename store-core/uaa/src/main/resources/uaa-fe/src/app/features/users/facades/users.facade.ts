import {Injectable, computed, inject, signal} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {TranslocoService} from '@jsverse/transloco';

import {ApiErrorService, snapshot} from '@cvhome-saas/ui-kit';
import {ToastService, type RoleOption} from '@cvhome-saas/ui-kit/ui';
import {
  AdminUserService,
  type AdminUserAction,
  type PlatformUserRow,
  type SessionSummary,
  type UserDto,
} from '@cvhome-saas/ui-kit/uaa';

import {newPairArray, pairRow} from '@shared/ui/pair-list/pair-list';

import {UsersApi} from '../services/users.api.service';

export const PAGE_SIZE = 20;

/** All a dialog needs to name the account it is about. `PlatformUserRow` and `UserDto` both fit. */
export interface UserTarget {
  readonly id: string;
  readonly username: string;
}

/**
 * Every account uaa knows about, and what a super admin may do to one.
 *
 * **No organization filter, unlike console-ui's platform users page.** That console asks "who is in
 * this org"; this one is uaa's own administration and lists everyone. uaa's list endpoint matches
 * metadata equality and offers no text query either, so there is no search box to add.
 *
 * **`SuperAdminImmutableException` is not predicted.** uaa refuses to disable or delete the
 * platform's own super admin, and which account that is belongs to uaa's configuration. The action
 * is offered and the server's refusal is rendered, rather than a control hidden on a guess.
 *
 * **Opening a user fetches it.** The list row is `PlatformUserRow`, which flattens the name and
 * keeps only `org` and `store` out of the metadata bag; the dialog edits the bag, so it needs the
 * `UserDto` the row was made from.
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

  /** What the dialog is editing: a fetched account, `'new'` while creating, or null when closed. */
  readonly editing = signal<UserDto | 'new' | null>(null);

  /** Still dialogs: handing over a password, and a delete that cannot be undone. */
  readonly resetting = signal<UserTarget | null>(null);
  readonly deleting = signal<UserTarget | null>(null);

  /**
   * The profile fields.
   *
   * `username` and `email` are on `CreateUserRequest` and **not** on `UpdateUserRequest`, so they
   * are editable while creating and read-only afterwards — see lessons.md, "Users — email and
   * username cannot be changed here".
   */
  readonly form = new FormGroup({
    username: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    email: new FormControl('', {nonNullable: true, validators: [Validators.email]}),
    firstName: new FormControl('', {nonNullable: true}),
    lastName: new FormControl('', {nonNullable: true}),
    enabled: new FormControl(true, {nonNullable: true}),
    metadata: newPairArray(),
  });

  /** Roles are a set of checkboxes over one value, so they are held beside the form. */
  readonly draftRoles = signal<readonly string[]>([]);

  /** The open account's live sessions, fetched with it; null while nothing is open or the leg failed. */
  readonly sessions = signal<readonly SessionSummary[] | null>(null);

  /**
   * Metadata keys that arrived from the server, whose remove buttons are disabled.
   *
   * uaa's `updateUser` does `u.getMetadata().putAll(req.metadata())` — a merge. A key that is
   * already stored cannot be removed through this API, and a remove button that silently does
   * nothing is worse than none. See lessons.md, "Users — metadata is merged, never replaced".
   */
  readonly lockedMetadataKeys = signal<readonly string[]>([]);

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

  dismiss(): void {
    this.editing.set(null);
    this.resetting.set(null);
    this.deleting.set(null);
    this.sessions.set(null);
  }

  /**
   * The sessions leg is allowed to fail on its own: the dialog still edits the account, and the section says the
   * list could not be loaded rather than the whole form blanking.
   */
  private loadSessions(id: string): void {
    this.sessions.set(null);
    this.admin.sessions(id).subscribe({
      next: (list) => this.sessions.set(list),
      error: () => this.sessions.set([]),
    });
  }

  /** Clears the lockout; the dialog re-reads the account so the status badge follows. */
  unlock(target: UserDto): void {
    this.busy.set(true);
    this.admin.unlock(target.id).subscribe({
      next: () => {
        this.busy.set(false);
        this.toast.success(this.transloco.translate('users.toast.unlocked', {name: target.username}));
        this.users.reload();
        this.refresh(target.id);
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  revokeSession(target: UserDto, session: SessionSummary): void {
    this.admin.revokeSession(target.id, session.id).subscribe({
      next: () => this.loadSessions(target.id),
      error: (failure: unknown) => this.apiErrors.notify(failure),
    });
  }

  /** Signs the account out everywhere. */
  revokeSessions(target: UserDto): void {
    this.admin.revokeSessions(target.id).subscribe({
      next: ({revoked}) => {
        this.toast.success(this.transloco.translate('users.toast.signedOut', {name: target.username, count: revoked}));
        this.loadSessions(target.id);
      },
      error: (failure: unknown) => this.apiErrors.notify(failure),
    });
  }

  private refresh(id: string): void {
    this.admin.findOne(id).subscribe({
      next: (user) => {
        if (this.editing() !== 'new' && this.editing()) {
          this.editing.set(user);
        }
      },
    });
  }

  /** Opening a row fetches it: the dialog edits the metadata bag the row model does not carry. */
  select(row: PlatformUserRow): void {
    this.busy.set(true);
    this.admin.findOne(row.id).subscribe({
      next: (user) => {
        this.busy.set(false);
        this.fill(user);
        this.editing.set(user);
        this.loadSessions(user.id);
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  startCreate(): void {
    this.form.reset({username: '', email: '', firstName: '', lastName: '', enabled: true});
    this.form.controls.metadata.clear();
    this.lockedMetadataKeys.set([]);
    this.draftRoles.set([]);
    this.editing.set('new');
  }

  /**
   * Deleting and setting a password close the form first, rather than stacking one modal on another.
   *
   * Two dialogs in the top layer at once leaves the operator looking at a form they can no longer
   * reach, and Escape then closes the wrong one.
   */
  askDelete(target: UserTarget): void {
    this.editing.set(null);
    this.deleting.set(target);
  }

  askReset(target: UserTarget): void {
    this.editing.set(null);
    this.resetting.set(target);
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

  addMetadata(): void {
    this.form.controls.metadata.push(pairRow());
    this.form.controls.metadata.markAsDirty();
  }

  removeMetadata(index: number): void {
    this.form.controls.metadata.removeAt(index);
    this.form.controls.metadata.markAsDirty();
  }

  /**
   * One request, not three.
   *
   * `updateUser` applies names, `enabled`, `metadata` and `roles` in a single transaction — and it
   * handles roles as clear-then-assign, which is exactly "these are the roles now". The add/remove
   * diff this replaced needed two more round trips and could leave a user with half a role set if
   * the second leg failed.
   */
  save(): void {
    const target = this.editing();
    if (!target || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    const metadata = pairsToMap(raw.metadata);
    this.busy.set(true);

    if (target === 'new') {
      this.admin
        .create({
          username: raw.username.trim(),
          email: raw.email.trim(),
          firstName: blankToNull(raw.firstName),
          lastName: blankToNull(raw.lastName),
          roles: this.draftRoles(),
          metadata,
        })
        .subscribe({
          next: (created) => {
            this.busy.set(false);
            this.editing.set(null);
            this.users.reload();
            this.toast.success(
              this.transloco.translate('users.toast.created', {name: created.username}),
            );
            /*
             * `createUser` never sets a password hash, so the account exists, is enabled, and
             * cannot sign in. Going straight to the password dialog is the second half of one
             * action rather than a step someone has to know about.
             */
            this.resetting.set(created);
          },
          error: (failure: unknown) => this.failed(failure),
        });
      return;
    }

    this.admin
      .update(target.id, {
        firstName: blankToNull(raw.firstName),
        lastName: blankToNull(raw.lastName),
        enabled: raw.enabled,
        roles: this.draftRoles(),
        metadata,
      })
      .subscribe({
        next: () => {
          this.busy.set(false);
          this.editing.set(null);
          this.toast.success(this.transloco.translate('users.toast.saved', {name: target.username}));
          this.users.reload();
        },
        error: (failure: unknown) => this.failed(failure),
      });
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

  unlockRow(row: PlatformUserRow): void {
    this.apply(row, {kind: 'unlock'}, 'users.toast.unlocked');
  }

  toggleEnabled(row: PlatformUserRow): void {
    this.apply(
      row,
      {kind: row.enabled ? 'disable' : 'enable'},
      row.enabled ? 'users.toast.disabled' : 'users.toast.enabled',
    );
  }

  private fill(user: UserDto): void {
    const metadata = this.form.controls.metadata;
    metadata.clear();
    for (const [key, value] of Object.entries(user.metadata ?? {})) {
      metadata.push(pairRow(key, typeof value === 'string' ? value : JSON.stringify(value)));
    }
    this.lockedMetadataKeys.set(Object.keys(user.metadata ?? {}));
    this.form.patchValue({
      username: user.username,
      email: user.email ?? '',
      firstName: user.firstName ?? '',
      lastName: user.lastName ?? '',
      enabled: user.enabled,
    });
    this.form.markAsPristine();
    this.draftRoles.set(user.roles ?? []);
  }

  /**
   * One write, then a re-read.
   *
   * The list is re-fetched rather than patched in place because most of these endpoints answer
   * `void`: what the page then shows is what uaa stored, not what this console assumed.
   */
  private apply(target: UserTarget, action: AdminUserAction, successKey: string): void {
    this.busy.set(true);
    this.admin.apply(target.id, action).subscribe({
      next: () => {
        this.busy.set(false);
        this.dismiss();
        this.toast.success(this.transloco.translate(successKey, {name: target.username}));
        this.users.reload();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  /**
   * Server-side validation lands on the form, not only in a toast.
   *
   * `applyToForm` and `clearServerErrorsOnChange` belong together — without the second the field
   * stays invalid after the operator has fixed it — and `ApiErrorService` pairs them itself.
   */
  private failed(failure: unknown): void {
    this.busy.set(false);
    this.apiErrors.applyToForm(failure, this.form);
    this.apiErrors.notify(failure);
  }
}

function pairsToMap(pairs: readonly {key: string; value: string}[]): Readonly<Record<string, string>> {
  const out: Record<string, string> = {};
  for (const pair of pairs) {
    const key = pair.key.trim();
    if (key) {
      out[key] = pair.value;
    }
  }
  return out;
}

const blankToNull = (value: string): string | null => (value.trim() ? value.trim() : null);
