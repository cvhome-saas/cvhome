import {Injectable, computed, inject, signal} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {TranslocoService} from '@jsverse/transloco';

import {ApiErrorService, snapshot, type KpiDatum} from '@cvhome-saas/ui-kit';
import {ToastService, type RoleOption, type TabItem} from '@cvhome-saas/ui-kit/ui';
import {
  AdminUserService,
  type AdminUserAction,
  type InvitationDto,
  type InvitationStatus,
  type IssuedLink,
  type PlatformUserRow,
  type SessionSummary,
  type UserDto,
  type UserStatus,
} from '@cvhome-saas/ui-kit/uaa';

import {newPairArray, pairRow} from '@shared/ui/pair-list/pair-list';

import {UsersApi} from '../services/users.api.service';

export const PAGE_SIZE = 20;

/** All a dialog needs to name the account it is about. `PlatformUserRow` and `UserDto` both fit. */
export interface UserTarget {
  readonly id: string;
  readonly username: string;
}

/** The status segments above the table. `ALL` is no filter. */
export type StatusFilter = 'ALL' | UserStatus;
export const STATUS_FILTERS: readonly StatusFilter[] = ['ALL', 'ACTIVE', 'PENDING', 'LOCKED', 'DISABLED'];

/** The two views of the page: the accounts, and the invitations that created some of them. */
export type UsersView = 'accounts' | 'invitations';

export const INVITATION_FILTERS: readonly (InvitationStatus | '')[] = ['PENDING', 'ACCEPTED', 'REVOKED', 'EXPIRED', ''];

/** A link the console has just been handed and will never see again once the dialog closes. */
export interface ShownLink {
  readonly kind: 'INVITATION' | 'PASSWORD_RESET';
  readonly issued: IssuedLink;
}

/**
 * Every account uaa knows about, and what a super admin may do to one.
 *
 * **Search and status are the server's.** uaa's list takes `q` (a contains over username, email
 * and name), `status` and `role`, so the box and the segments here re-query rather than filter a
 * page — the realm has more accounts than one page holds. The counts above the table come from
 * their own endpoint and are re-read after every write, so a tile never disagrees with the list.
 *
 * **An invitation is the way a new account gets its first password.** `invite` creates the account
 * pending and answers with a one-time link, which the console shows once in a dialog of its own,
 * because only the token's hash is stored and nothing on the platform emails it. A reset link is
 * the same mechanism for an account that already has a password. See lessons.md, "Users — creating
 * an account is two calls, and there are no invites".
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
  readonly query = signal('');
  readonly status = signal<StatusFilter>('ALL');
  readonly view = signal<UsersView>('accounts');

  readonly invitationPage = signal(0);
  readonly invitationStatus = signal<InvitationStatus | ''>('PENDING');

  /** What the dialog is editing: a fetched account, `'new'` while creating, or null when closed. */
  readonly editing = signal<UserDto | 'new' | null>(null);
  /** The invite dialog: open or not. Its form is {@link inviteForm}. */
  readonly inviting = signal(false);

  /** Still dialogs: handing over a password, a delete that cannot be undone, a link shown once. */
  readonly resetting = signal<UserTarget | null>(null);
  readonly deleting = signal<UserTarget | null>(null);
  readonly issuingResetLink = signal<UserDto | null>(null);
  readonly resetLinkRevokes = signal(false);
  readonly revokingInvitation = signal<InvitationDto | null>(null);
  readonly shownLink = signal<ShownLink | null>(null);

  /**
   * The profile fields.
   *
   * `username` is on `CreateUserRequest` and **not** on `UpdateUserRequest`: it is the identity a
   * JWT `sub` carries, so it is editable while creating and read-only afterwards. `email` is
   * editable both ways since phase 4 — saving a changed address marks it unverified again.
   */
  readonly form = new FormGroup({
    username: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
    email: new FormControl('', {nonNullable: true, validators: [Validators.email]}),
    firstName: new FormControl('', {nonNullable: true}),
    lastName: new FormControl('', {nonNullable: true}),
    enabled: new FormControl(true, {nonNullable: true}),
    metadata: newPairArray(),
  });

  /** The invitation's fields. The username is optional and defaults to the email on the server. */
  readonly inviteForm = new FormGroup({
    email: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.email]}),
    username: new FormControl('', {nonNullable: true}),
    firstName: new FormControl('', {nonNullable: true}),
    lastName: new FormControl('', {nonNullable: true}),
    metadata: newPairArray(),
  });

  /** Roles are a set of checkboxes over one value, so they are held beside each form. */
  readonly draftRoles = signal<readonly string[]>([]);
  readonly inviteRoles = signal<readonly string[]>([]);

  /** The open account's live sessions, fetched with it; null while nothing is open or the leg failed. */
  readonly sessions = signal<readonly SessionSummary[] | null>(null);

  /**
   * Metadata keys that came back from the server for the open account.
   *
   * uaa merges metadata on update, and a key sent with `null` is removed — so a row the operator
   * deletes has to go out as `key: null` rather than simply not go out. This is the list to diff
   * against on save. See lessons.md, "Users — metadata is merged, never replaced".
   */
  private storedMetadataKeys: readonly string[] = [];

  private readonly users = snapshot(
    () => ({page: this.pageIndex(), count: PAGE_SIZE, q: this.query(), status: this.statusParam()}),
    (query) => this.api.load(query),
  );

  private readonly counts = snapshot(
    () => ({}),
    () => this.api.counts(),
  );

  private readonly invitations = snapshot(
    () => ({page: this.invitationPage(), count: PAGE_SIZE, status: this.invitationStatus()}),
    (query) => this.api.invitations(query.page, query.count, query.status),
  );

  private readonly roles = snapshot(
    () => ({}),
    () => this.api.assignableRoles(),
  );

  readonly isLoading = this.users.isLoading;
  readonly error = this.users.error;
  readonly isEmpty = this.users.isEmpty;
  readonly reload = () => this.refreshAll();

  readonly rows = computed(() => this.users.value()?.rows ?? []);
  readonly totalElements = computed(() => this.users.value()?.totalElements ?? 0);
  readonly totalPages = computed(() => this.users.value()?.totalPages ?? 0);

  readonly invitationRows = computed(() => this.invitations.value()?.content ?? []);
  readonly invitationTotalElements = computed(() => this.invitations.value()?.totalElements ?? 0);
  readonly invitationTotalPages = computed(() => this.invitations.value()?.totalPages ?? 0);
  readonly invitationsLoading = this.invitations.isLoading;

  readonly roleOptions = computed<readonly RoleOption[]>(() =>
    (this.roles.value() ?? []).map((role) => ({value: role, label: role})),
  );

  /**
   * The tiles: total with the active share, pending invitations, locked, disabled.
   *
   * Each of the four `computed`s below reads `activeLang()` first: `translate` is not reactive, so
   * without that dependency a language switch would leave the tiles and tabs in the old language
   * until the counts happened to reload.
   */
  readonly tiles = computed<readonly KpiDatum[]>(() => {
    this.transloco.activeLang();
    const c = this.counts.value();
    const t = (key: string, params?: Record<string, unknown>) => this.transloco.translate(`users.tiles.${key}`, params);
    if (!c) {
      return [];
    }
    return [
      {label: t('total'), value: String(c.total), icon: 'users', tone: 'blue', flag: t('activeOf', {count: c.active})},
      {label: t('pending'), value: String(c.pending), icon: 'envelope', tone: 'amber', flag: t('pendingHint')},
      {label: t('locked'), value: String(c.locked), icon: 'lock', tone: c.locked ? 'red' : 'slate', flag: t('lockedHint')},
      {label: t('disabled'), value: String(c.disabled), icon: 'xCircle', tone: 'slate', flag: t('disabledHint')},
    ];
  });

  /** The status segments, each carrying its count so the operator sees what a click will show. */
  readonly statusTabs = computed<readonly TabItem[]>(() => {
    this.transloco.activeLang();
    const c = this.counts.value();
    const badge = (n: number | undefined) => (n === undefined ? undefined : String(n));
    return STATUS_FILTERS.map((key) => ({
      key,
      label: this.transloco.translate(`users.filter.${key}`),
      badge: badge(
        key === 'ALL' ? c?.total : key === 'ACTIVE' ? c?.active : key === 'PENDING' ? c?.pending : key === 'LOCKED' ? c?.locked : c?.disabled,
      ),
      badgeTone: key === 'LOCKED' && c?.locked ? 'red' : 'slate',
    }));
  });

  readonly viewTabs = computed<readonly TabItem[]>(() => {
    this.transloco.activeLang();
    const pending = this.counts.value()?.pending;
    return [
      {key: 'accounts', label: this.transloco.translate('users.view.accounts')},
      {
        key: 'invitations',
        label: this.transloco.translate('users.view.invitations'),
        badge: pending ? String(pending) : undefined,
        badgeTone: 'amber',
      },
    ];
  });

  readonly invitationTabs = computed<readonly TabItem[]>(() => {
    this.transloco.activeLang();
    return INVITATION_FILTERS.map((key) => ({key, label: this.transloco.translate(`users.invitations.filter.${key || 'ALL'}`)}));
  });

  private statusParam(): UserStatus | '' {
    const status = this.status();
    return status === 'ALL' ? '' : status;
  }

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }

  goToInvitationPage(page: number): void {
    this.invitationPage.set(page);
  }

  /** A new term or segment starts from the first page: page 4 of a narrower result is usually empty. */
  setQuery(value: string): void {
    this.query.set(value);
    this.pageIndex.set(0);
  }

  setStatus(value: string): void {
    this.status.set(value as StatusFilter);
    this.pageIndex.set(0);
  }

  setView(value: string): void {
    this.view.set(value as UsersView);
  }

  setInvitationStatus(value: string): void {
    this.invitationStatus.set(value as InvitationStatus | '');
    this.invitationPage.set(0);
  }

  dismiss(): void {
    this.editing.set(null);
    this.inviting.set(false);
    this.resetting.set(null);
    this.deleting.set(null);
    this.issuingResetLink.set(null);
    this.revokingInvitation.set(null);
    this.sessions.set(null);
  }

  /** Closing the link dialog is the one dismissal that must not also close anything else: the link is gone. */
  dismissLink(): void {
    this.shownLink.set(null);
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
        this.refreshAll();
        this.refresh(target.id);
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  /** An operator vouching for the address; the badge follows the re-read. */
  verifyEmail(target: UserDto): void {
    this.busy.set(true);
    this.admin.verifyEmail(target.id).subscribe({
      next: (user) => {
        this.busy.set(false);
        this.editing.set(user);
        this.toast.success(this.transloco.translate('users.toast.emailVerified', {name: target.username}));
        this.refreshAll();
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
    this.storedMetadataKeys = [];
    this.draftRoles.set([]);
    this.editing.set('new');
  }

  startInvite(): void {
    this.inviteForm.reset({email: '', username: '', firstName: '', lastName: ''});
    this.inviteForm.controls.metadata.clear();
    this.inviteRoles.set([]);
    this.inviting.set(true);
  }

  /**
   * Deleting, setting a password and issuing a link close the form first, rather than stacking one
   * modal on another.
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

  askResetLink(target: UserDto): void {
    this.editing.set(null);
    this.resetLinkRevokes.set(false);
    this.issuingResetLink.set(target);
  }

  askRevokeInvitation(invitation: InvitationDto): void {
    this.revokingInvitation.set(invitation);
  }

  toggleRole(role: string): void {
    this.draftRoles.set(toggled(this.draftRoles(), role));
  }

  toggleInviteRole(role: string): void {
    this.inviteRoles.set(toggled(this.inviteRoles(), role));
  }

  addMetadata(): void {
    this.form.controls.metadata.push(pairRow());
    this.form.controls.metadata.markAsDirty();
  }

  removeMetadata(index: number): void {
    this.form.controls.metadata.removeAt(index);
    this.form.controls.metadata.markAsDirty();
  }

  addInviteMetadata(): void {
    this.inviteForm.controls.metadata.push(pairRow());
  }

  removeInviteMetadata(index: number): void {
    this.inviteForm.controls.metadata.removeAt(index);
  }

  /**
   * One request, not three.
   *
   * `updateUser` applies names, email, `enabled`, `metadata` and `roles` in a single transaction —
   * and it handles roles as clear-then-assign, which is exactly "these are the roles now". Metadata
   * merges, so a row the operator removed goes out as `null`, which is the server's word for
   * "unset". `email` is sent only when it changed: sending it unchanged would still re-mark the
   * address unverified.
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
            this.refreshAll();
            this.toast.success(this.transloco.translate('users.toast.created', {name: created.username}));
            /*
             * `createUser` without a password leaves the account unable to sign in. Going straight
             * to the password dialog is the second half of one action rather than a step someone
             * has to know about. Invite is the alternative that hands the choice to the person.
             */
            this.resetting.set(created);
          },
          error: (failure: unknown) => this.failed(failure, this.form),
        });
      return;
    }

    const removed = Object.fromEntries(
      this.storedMetadataKeys.filter((key) => !(key in metadata)).map((key) => [key, null]),
    );
    const email = raw.email.trim();
    this.admin
      .update(target.id, {
        firstName: blankToNull(raw.firstName),
        lastName: blankToNull(raw.lastName),
        ...(email && email !== (target.email ?? '') ? {email} : {}),
        enabled: raw.enabled,
        roles: this.draftRoles(),
        metadata: {...metadata, ...removed},
      })
      .subscribe({
        next: () => {
          this.busy.set(false);
          this.editing.set(null);
          this.toast.success(this.transloco.translate('users.toast.saved', {name: target.username}));
          this.refreshAll();
        },
        error: (failure: unknown) => this.failed(failure, this.form),
      });
  }

  /** Creates the account pending and hands the one-time link to the dialog that shows it once. */
  invite(): void {
    if (this.inviteForm.invalid) {
      this.inviteForm.markAllAsTouched();
      return;
    }
    const raw = this.inviteForm.getRawValue();
    this.busy.set(true);
    this.admin
      .invite({
        email: raw.email.trim(),
        username: blankToNull(raw.username),
        firstName: blankToNull(raw.firstName),
        lastName: blankToNull(raw.lastName),
        roles: this.inviteRoles(),
        metadata: pairsToMap(raw.metadata),
      })
      .subscribe({
        next: (issued) => this.linkIssued('INVITATION', issued, 'users.toast.invited'),
        error: (failure: unknown) => this.failed(failure, this.inviteForm),
      });
  }

  /** A fresh link; the previous one is revoked by the server. */
  resendInvitation(invitation: InvitationDto): void {
    this.busy.set(true);
    this.admin.resendInvitation(invitation.userId).subscribe({
      next: (issued) => this.linkIssued('INVITATION', issued, 'users.toast.resent'),
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  confirmRevokeInvitation(): void {
    const invitation = this.revokingInvitation();
    if (!invitation) {
      return;
    }
    this.busy.set(true);
    this.admin.revokeInvitation(invitation.userId).subscribe({
      next: () => {
        this.busy.set(false);
        this.dismiss();
        this.toast.success(this.transloco.translate('users.toast.invitationRevoked', {email: invitation.email}));
        this.refreshAll();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  confirmResetLink(): void {
    const target = this.issuingResetLink();
    if (!target) {
      return;
    }
    this.busy.set(true);
    this.admin.createResetLink(target.id, {revokeSessions: this.resetLinkRevokes()}).subscribe({
      next: (issued) => this.linkIssued('PASSWORD_RESET', issued, 'users.toast.resetLinkIssued'),
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  private linkIssued(kind: ShownLink['kind'], issued: IssuedLink, toastKey: string): void {
    this.busy.set(false);
    this.dismiss();
    this.toast.success(this.transloco.translate(toastKey, {name: issued.user.username}));
    this.refreshAll();
    this.shownLink.set({kind, issued});
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
    this.storedMetadataKeys = Object.keys(user.metadata ?? {});
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
        this.refreshAll();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  /** Every write re-reads all three: the list, the tiles and the invitations, so none can disagree. */
  private refreshAll(): void {
    this.users.reload();
    this.counts.reload();
    this.invitations.reload();
  }

  /**
   * Server-side validation lands on the form, not only in a toast.
   *
   * `applyToForm` and `clearServerErrorsOnChange` belong together — without the second the field
   * stays invalid after the operator has fixed it — and `ApiErrorService` pairs them itself.
   */
  private failed(failure: unknown, form: FormGroup): void {
    this.busy.set(false);
    this.apiErrors.applyToForm(failure, form);
    this.apiErrors.notify(failure);
  }
}

function toggled(held: readonly string[], role: string): readonly string[] {
  return held.includes(role) ? held.filter((r) => r !== role) : [...held, role];
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
