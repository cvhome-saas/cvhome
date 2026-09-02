import {DestroyRef, Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {ApiErrorService, clearServerErrorsOnChange, AuthService, snapshot} from '@cvhome-saas/ui-kit';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {humanizeStatus} from '@models/orders';
import type {InvitationStatus} from '@models/users';
import {INVITATION_STATUSES, USERS_TABS, type InvitationRow, type IssuedInvitation, type TeamRow, type UsersTab} from '@models/team';
import {RoleLabel} from '@shared/i18n/role-label';
import {ConsolePermissions} from '@shared/auth/console-permissions';
import type {TabItem} from '@cvhome-saas/ui-kit/ui';
import {ToastService} from '@cvhome-saas/ui-kit/ui';
import {UsersApi} from '../services/users.api.service';
import {UserFormService, type UserForm} from '../services/user-form.service';

export const PAGE_SIZE = 20;

/** What the detail rail is doing: reading a user, editing one, or creating one. */
export type RailMode = 'view' | 'edit' | 'create';

/**
 * The team page.
 *
 * One load, and that is the shape of the whole module: `user-account/list` answers the rows, the
 * roles a form may assign ride along as an optional leg, and the two KPI figures are read off what
 * the list already returned rather than fetched. There is no counts key here as there is on the
 * payments ledger, because there is nothing separate to count.
 *
 * **The list is scoped to the open store, and that hides people.** `UserAccountApi.list` filters uaa
 * on `{org, store}`, so an account whose metadata carries no store — which is how an org admin is
 * stored — appears in no store's list at all, including their own. The page says so rather than
 * letting an operator conclude the team is smaller than it is. See lessons.md, "Users — the user
 * list is store-scoped, so an org admin is in no list".
 */
@Injectable()
export class UsersFacade {
  private readonly api = inject(UsersApi);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly auth = inject(AuthService);
  private readonly permissions = inject(ConsolePermissions);
  private readonly roleLabels = inject(RoleLabel);
  private readonly forms = inject(UserFormService);
  private readonly destroyRef = inject(DestroyRef);

  /** The signed-in username. The only identity shared with a row — the JWT carries no user id. */
  private readonly self = this.auth.getCachedAuthUser()?.username ?? '';

  readonly busy = signal(false);

  /** Which user the rail is showing, by uaa id. Mirrored into the URL by the page. */
  readonly selectedId = signal<string | null>(null);
  readonly railMode = signal<RailMode>('view');

  /** The confirmation dialogs' subjects, or null. */
  readonly deleting = signal<TeamRow | null>(null);
  readonly resetting = signal<TeamRow | null>(null);

  readonly canManageUsers = this.permissions.canManageUsers();

  /**
   * The page being read.
   *
   * A `linkedSignal` over the store, so switching stores drops the reader back to the first page
   * rather than asking for page 4 of a smaller team.
   */
  readonly pageIndex = linkedSignal<unknown, number>({
    source: () => this.shell.currentStoreId(),
    computation: () => 0,
  });

  /**
   * What the table is a reading of.
   *
   * `undefined` until the store is known, which leaves the resource idle. The store directory
   * resolves a moment after the page first renders, and without this gate the page would fire two
   * requests on every open — one unscoped, one correct.
   */
  private readonly query = computed(() => {
    const storeId = this.shell.currentStoreId();
    if (!storeId) {
      return undefined;
    }
    return {page: {page: this.pageIndex(), count: PAGE_SIZE}, self: this.self, storeId};
  });

  private readonly team = snapshot(
    () => this.query(),
    (query) => this.api.loadTeam(query),
  );

  readonly isLoading = this.team.isLoading;
  readonly error = this.team.error;
  readonly isEmpty = this.team.isEmpty;

  readonly rows = computed<readonly TeamRow[]>(() => this.team.value()?.rows ?? []);
  readonly totalElements = computed(() => this.team.value()?.totalElements ?? 0);
  readonly totalPages = computed(() => this.team.value()?.totalPages ?? 0);

  /**
   * The roles a picker may offer.
   *
   * Falls back to the roles the selected user already holds when the lookup failed, so an edit form
   * still shows the truth about that user rather than an empty set that would read as "no roles".
   */
  readonly assignableRoles = computed<readonly string[]>(() => {
    const offered = this.team.value()?.assignableRoles ?? [];
    return offered.length ? offered : (this.selected()?.roles ?? []);
  });

  readonly selected = computed<TeamRow | null>(() => {
    const id = this.selectedId();
    return id ? (this.rows().find((row) => row.id === id) ?? null) : null;
  });

  /**
   * The create/edit form, or null while the rail is only reading.
   *
   * Held as state and built by `startCreate`/`startEdit`, **not** derived in a `computed`. A computed
   * over `railMode()` and `selected()` re-runs whenever the row list reloads — the rows are new
   * objects every time — so a half-typed form would be silently rebuilt underneath the operator. It
   * also made `clearServerErrorsOnChange` subscribe again on every re-run.
   */
  private readonly formState = signal<UserForm | null>(null);
  readonly form = this.formState.asReadonly();

  readonly heading = computed(() => {
    this.transloco.activeLang();
    return {
      title: this.transloco.translate('users.heading.title'),
      context: this.transloco.translate('users.heading.context', {
        store: this.shell.currentStore()?.name ?? '',
      }),
    };
  });

  /* ----------------------------------------------------------- the invitations tab ---- */

  readonly activeTab = signal<UsersTab>('team');

  readonly canManageInvitations = this.permissions.canManageInvitations();

  /**
   * The invitations, on their own key.
   *
   * Keyed on the store only so that it re-reads when the console changes tenant, and **not** on the
   * active tab: switching back to Team and forward again should not re-ask a question whose answer
   * did not move. `OrgMemberApi` is org-scoped, so the store is a proxy for the org rather than a
   * filter — there is no org id on the client to key on.
   *
   * Gated on the permission as well: `OrgMemberApi` is org-admin-only class-wide, so for a store
   * admin this would be a guaranteed 403 on every page load.
   */
  private readonly invitations = snapshot(
    () => (this.canManageInvitations ? (this.shell.currentStoreId() ?? undefined) : undefined),
    () => this.api.loadInvitations(),
  );

  readonly invitationRows = computed<readonly InvitationRow[]>(() => this.invitations.value()?.rows ?? []);
  readonly invitationsLoading = this.invitations.isLoading;
  readonly invitationsError = this.invitations.error;

  /**
   * The tab strip.
   *
   * Invitations carries the pending count as a badge — it is the one tab that is a to-do list rather
   * than a view, the same reason the payments ledger badges its approval queue.
   */
  readonly tabs = computed<readonly TabItem[]>(() => {
    this.transloco.activeLang();
    const waiting = this.pendingInvitations();
    return USERS_TABS.filter((tab) => tab !== 'invitations' || this.canManageInvitations).map((tab) => ({
      key: tab,
      label: this.transloco.translate(`users.tab.${tab}`),
      badge:
        tab === 'invitations' && waiting
          ? this.localeFormat.localizeNumber(waiting, 'decimal')
          : undefined,
      badgeTone: tab === 'invitations' && waiting ? ('amber' as const) : undefined,
    }));
  });

  /**
   * The link an operator has to hand over, or null.
   *
   * Held here because it exists **exactly once**: the token is returned in the response that created
   * the invitation and only its hash is stored, so nothing can fetch it again. Closing the dialog
   * loses it for good, which is why the dialog says so. See lessons.md, "Users — nothing emails an
   * invitation".
   */
  readonly issued = signal<IssuedInvitation | null>(null);

  /** Whether the invite dialog is open. */
  readonly inviting = signal(false);

  /** The invitation queued for revocation, or null. */
  readonly revoking = signal<InvitationRow | null>(null);

  startInvite(): void {
    this.inviting.set(true);
  }

  dismissInvite(): void {
    this.inviting.set(false);
  }

  dismissIssued(): void {
    this.issued.set(null);
  }

  askToRevoke(row: InvitationRow): void {
    this.revoking.set(row);
  }

  invite(email: string, role: string): void {
    this.sendInvitation(email, role, false);
  }

  /**
   * Issues a fresh token and invalidates the previous one.
   *
   * Not "show me that link again", which is impossible — only the hash is stored. A link that went
   * astray should stop working, so resending is a rotation, and the dialog that follows carries the
   * new link exactly as the first one did.
   */
  resend(row: InvitationRow): void {
    this.sendInvitation(row.email, row.role, true);
  }

  confirmRevoke(): void {
    const row = this.revoking();
    if (!row || this.busy()) {
      return;
    }
    this.busy.set(true);
    this.api.revoke(row.id).subscribe({
      next: () => {
        this.busy.set(false);
        this.revoking.set(null);
        this.toast.success(this.transloco.translate('users.toast.revoked', {email: row.email}));
        this.invitations.reload();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.toast.danger(this.apiErrors.messageFor(failure));
      },
    });
  }

  private sendInvitation(email: string, role: string, resending: boolean): void {
    if (this.busy()) {
      return;
    }
    this.busy.set(true);
    const request = resending ? this.api.resend(email, role) : this.api.invite(email, role);
    request.subscribe({
      next: ({token, expiresAt}) => {
        this.busy.set(false);
        this.inviting.set(false);
        // The link is assembled against the console's own origin: the token is meaningless anywhere else.
        this.issued.set({email, role, link: this.acceptLink(token), expiresAt});
        this.invitations.reload();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.toast.danger(this.apiErrors.messageFor(failure));
      },
    });
  }

  private acceptLink(token: string): string {
    const origin = typeof window === 'undefined' ? '' : window.location.origin;
    return `${origin}/accept-invitation?token=${encodeURIComponent(token)}`;
  }

  /**
   * How many invitations are outstanding.
   *
   * `null` until the invitation list has loaded, and for a store admin forever — the endpoint is
   * org-admin-only, so they cannot be told. An em dash rather than a zero either way, because
   * "nobody is waiting to join" is a claim the page has not earned in either case.
   */
  readonly pendingInvitations = computed<number | null>(() => this.invitations.value()?.pending ?? null);

  /** The open store's name, for the scope notice and the rail's "store access" fact. */
  storeName(): string {
    return this.shell.currentStore()?.name ?? '';
  }

  /** What the table is showing right now, under the panel title. */
  readonly subtitle = computed(() => {
    this.transloco.activeLang();
    const total = this.totalElements();
    const shown = this.rows().length;
    if (!shown) {
      return this.transloco.translate('users.subtitle.none');
    }
    const digits = (value: number) => this.localeFormat.localizeNumber(value, 'decimal');
    const from = this.pageIndex() * PAGE_SIZE + 1;
    return this.transloco.translate('users.subtitle.range', {
      from: digits(from),
      to: digits(from + shown - 1),
      total: digits(total),
      count: total,
    });
  });

  /** A monogram, from the name where there is one and the username where there is not. */
  initialsOf(row: TeamRow): string {
    const source = row.firstName || row.lastName ? `${row.firstName} ${row.lastName}` : row.userName;
    return source
      .split(/[\s._-]+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part.charAt(0).toUpperCase())
      .join('');
  }

  /** Several roles, in the reader's language, through the known-set guard. */
  roleList(roles: readonly string[]): string {
    return this.roleLabels.labels(roles);
  }

  /** The active toggle inside the form, which is a value rather than an immediate write. */
  setActiveFlag(active: boolean): void {
    const control = this.form()?.controls.active;
    control?.setValue(active);
    control?.markAsDirty();
  }

  roleLabel(role: string): string {
    return this.roleLabels.label(role);
  }

  /** Whether the form currently holds a role. Drives the picker's checkboxes. */
  hasRole(role: string): boolean {
    return (this.form()?.controls.roles.value ?? []).includes(role);
  }

  /**
   * Adds or removes a role.
   *
   * Marked dirty by hand: a `FormControl` holding an array is set rather than typed into, so nothing
   * else would tell the form it had changed and Save would stay disabled.
   */
  toggleRole(role: string): void {
    const control = this.form()?.controls.roles;
    if (!control) {
      return;
    }
    const current = control.value;
    control.setValue(current.includes(role) ? current.filter((held) => held !== role) : [...current, role]);
    control.markAsDirty();
    control.markAsTouched();
  }

  /* ------------------------------------------------------------------- the rail ---- */

  /** Reads a user in the rail, by id — the page holds the URL, the facade holds the state. */
  selectRow(id: string): void {
    this.selectedId.set(id);
    this.railMode.set('view');
    this.formState.set(null);
  }

  clearSelection(): void {
    this.selectedId.set(null);
    this.railMode.set('view');
    this.formState.set(null);
  }

  startCreate(): void {
    this.selectedId.set(null);
    this.railMode.set('create');
    this.formState.set(this.newForm('create'));
  }

  startEdit(): void {
    const row = this.selected();
    if (!row) {
      return;
    }
    const form = this.newForm('edit');
    this.forms.patchFrom(form, row);
    this.railMode.set('edit');
    this.formState.set(form);
  }

  cancelEdit(): void {
    this.railMode.set('view');
    this.formState.set(null);
  }

  /** Always together: a server error is not a validator, so nothing else will ever remove it. */
  private newForm(mode: 'create' | 'edit'): UserForm {
    const form = this.forms.build(mode);
    clearServerErrorsOnChange(form, this.destroyRef);
    return form;
  }

  /* ----------------------------------------------------------------- the writes ---- */

  /**
   * Creates or updates, then re-reads.
   *
   * The endpoints answer the saved user, and the page deliberately ignores it: a re-read shows what
   * the server normalised — a lowercased email, a role set it filtered — rather than what the
   * operator typed.
   */
  save(): void {
    const form = this.form();
    const mode = this.railMode();
    if (!form || this.busy() || mode === 'view') {
      return;
    }
    if (form.invalid) {
      form.markAllAsTouched();
      return;
    }

    const value = form.getRawValue();
    const user = {
      userName: value.userName,
      emailAddress: value.emailAddress,
      firstName: value.firstName || null,
      lastName: value.lastName || null,
      active: value.active,
      roles: [...value.roles],
    };

    this.busy.set(true);
    const request =
      mode === 'create'
        ? this.api.create({...user, password: value.password})
        : this.api.update({...user, id: this.selected()?.id});

    request.subscribe({
      next: () => {
        this.busy.set(false);
        this.railMode.set('view');
        this.formState.set(null);
        this.toast.success(
          this.transloco.translate(mode === 'create' ? 'users.toast.created' : 'users.toast.updated', {
            name: value.userName,
          }),
        );
        this.team.reload();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        /*
         * A taken username or email arrives as a 409 with no `fieldErrors[]` — the same shape the
         * signup flow hits, and for the same reason. `applyFieldErrors` binds whatever the server
         * did name; the toast carries the rest.
         */
        this.apiErrors.applyToForm(failure, form);
      },
    });
  }

  /** Enable or disable. Re-reads rather than flipping the row, which is what seller-ui did. */
  toggleActive(row: TeamRow): void {
    if (this.busy()) {
      return;
    }
    this.busy.set(true);
    this.api.setActive(row.id, !row.active).subscribe({
      next: () => {
        this.busy.set(false);
        this.toast.success(
          this.transloco.translate(row.active ? 'users.toast.disabled' : 'users.toast.enabled', {
            name: row.name,
          }),
        );
        this.team.reload();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.toast.danger(this.apiErrors.messageFor(failure));
      },
    });
  }

  askToDelete(row: TeamRow): void {
    this.deleting.set(row);
  }

  askToResetPassword(row: TeamRow): void {
    this.resetting.set(row);
  }

  dismissDialogs(): void {
    this.deleting.set(null);
    this.resetting.set(null);
  }

  confirmDelete(): void {
    const row = this.deleting();
    if (!row || this.busy()) {
      return;
    }
    this.busy.set(true);
    this.api.delete(row.id).subscribe({
      next: () => {
        this.busy.set(false);
        this.deleting.set(null);
        if (this.selectedId() === row.id) {
          this.selectedId.set(null);
        }
        this.toast.success(this.transloco.translate('users.toast.deleted', {name: row.name}));
        this.team.reload();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.toast.danger(this.apiErrors.messageFor(failure));
      },
    });
  }

  /**
   * Sets a user's password.
   *
   * The endpoint this calls was refused for **every** caller until the permission token behind it
   * was given a case in `CustomPermissionEvaluator`; a 403 here now means the operator genuinely
   * lacks the role, which is a different and truthful answer.
   *
   * There is no current-password field because nothing verifies one, and no "email them the new
   * password" because nothing on the platform sends email.
   */
  confirmReset(password: string): void {
    const row = this.resetting();
    if (!row || this.busy()) {
      return;
    }
    this.busy.set(true);
    this.api.resetPassword(row.id, password).subscribe({
      next: () => {
        this.busy.set(false);
        this.resetting.set(null);
        this.toast.success(this.transloco.translate('users.toast.passwordReset', {name: row.name}));
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.toast.danger(this.apiErrors.messageFor(failure));
      },
    });
  }

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }

  reload(): void {
    this.team.reload();
  }

  reloadInvitations(): void {
    this.invitations.reload();
  }

  /**
   * An invitation's status, in the reader's language.
   *
   * `InvitationStatus` is a real Java enum rather than a database table, so it cannot grow the way
   * a role can — but it goes through the same known-set guard as everything else the server names,
   * because Transloco throws on a missing key and a fifth value would take the tab down.
   */
  invitationStatusLabel(status: InvitationStatus): string {
    this.transloco.activeLang();
    return INVITATION_STATUSES.has(status)
      ? this.transloco.translate(`users.invitationStatus.${status}`)
      : humanizeStatus(status);
  }

}
