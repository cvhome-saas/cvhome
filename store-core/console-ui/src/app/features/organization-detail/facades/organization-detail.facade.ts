import {Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import type {AdminUserAction} from '@api/uaa/admin-user.service';
import {ApiErrorService} from '@core/errors/api-error.service';
import {
  ORG_STATUS_TONE,
  PROVISIONING_STATE_TONE,
  STORE_STATUS_TONE,
  SUBSCRIPTION_STATUS_TONE,
  type OrgRow,
  type PlatformStoreRow,
  type PlatformUserRow,
} from '@models/platform';
import type {Tone} from '@models/ui';
import {INVOICE_STATUS_TONE} from '@models/platform-billing';
import {Money} from '@shared/i18n/money';
import {PlatformLabel} from '@shared/i18n/platform-label';
import {RoleLabel} from '@shared/i18n/role-label';
import {snapshot} from '@shared/state/snapshot';
import type {NavSection} from '@shared/ui/section-nav/section-nav';
import type {RoleChange, RoleOption} from '@shared/ui/roles-dialog/roles-dialog';
import {ToastService} from '@shared/ui/toast/toast';
import {OrganizationDetailApi} from '../services/organization-detail.api.service';

export const PAGE_SIZE = 20;

/** How many rows the billing tab's two short lists show before deferring to the ledger. */
const BILLING_ROWS = 8;

/** Minor units to major, as everything billing sends is in minor units. */
const MINOR_UNITS = 100;

/**
 * The tabs, in the order the rail renders them.
 *
 * `billing` sits after the accounts because it is the last thing an operator reaches for and the
 * first one they arrive for during a dispute — either way it is a leaf, not a starting point.
 * `activity` stays last because it is still empty: nothing reads `tenancy_audit`.
 */
export const ORG_SECTIONS: readonly NavSection[] = [
  {key: 'overview', labelKey: 'platform.organization.section.overview', icon: 'building'},
  {key: 'stores', labelKey: 'platform.organization.section.stores', icon: 'shoppingCart'},
  {key: 'users', labelKey: 'platform.organization.section.users', icon: 'users'},
  {key: 'billing', labelKey: 'platform.organization.section.billing', icon: 'dollar'},
  {key: 'activity', labelKey: 'platform.organization.section.activity', icon: 'clock'},
];

export type OrgSection = 'overview' | 'stores' | 'users' | 'billing' | 'activity';

/** Which confirmation is open, if any. */
export type OrgPrompt = 'suspend' | 'resume' | 'close' | null;

/**
 * The toast each lever raises, written out rather than built from the prompt name.
 *
 * `` `…toast.${prompt}ed` `` reads naturally and produces `resumeed` and `closeed`. Transloco is
 * configured to throw on a missing key, so that is a page that goes down on a successful write —
 * the worst moment to fail. A map cannot be spelled wrong without failing the lint that reads it.
 */
const LIFECYCLE_TOAST: Readonly<Record<'suspend' | 'resume' | 'close', string>> = {
  suspend: 'platform.organization.toast.suspended',
  resume: 'platform.organization.toast.resumed',
  close: 'platform.organization.toast.closed',
};

/**
 * One organization: who it is, what it owns, who administers it, and the levers over it.
 *
 * **Four loads, three of which are lazy.** The identity is the page and always loads; the stores and
 * the accounts load when their tab is opened and not before, keyed on the tab so switching back and
 * forth does not re-ask. Activity loads nothing — no endpoint reads `tenancy_audit`.
 *
 * **Illegal transitions are not predicted.** `OrgLifecycleService` holds the legal moves in a table
 * and answers `IllegalLifecycleTransitionException` for the rest; the header offers what the current
 * status makes sensible and lets the server refuse the rest, because the console guessing at that
 * table is how the two come to disagree.
 */
@Injectable()
export class OrganizationDetailFacade {
  private readonly api = inject(OrganizationDetailApi);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly labels = inject(PlatformLabel);
  private readonly money = inject(Money);
  private readonly roleLabels = inject(RoleLabel);

  /** The organization being read, set by the page from the route. */
  readonly orgId = signal<string | null>(null);
  readonly section = signal<OrgSection>('overview');

  readonly busy = signal(false);

  /** Which confirmation is open. */
  readonly prompt = signal<OrgPrompt>(null);
  /** The operator's stated reason for a suspension. Optional; the server defaults it. */
  readonly suspendReason = signal('');

  /** Whether the rename field is being edited, and what is in it. */
  readonly renaming = signal(false);
  readonly draftName = signal('');

  /** Whether the owner password dialog is open. */
  readonly resettingOwner = signal(false);

  /** The account a user-tab dialog is acting on, or null. */
  readonly resettingUser = signal<PlatformUserRow | null>(null);
  readonly editingRoles = signal<PlatformUserRow | null>(null);
  readonly deletingUser = signal<PlatformUserRow | null>(null);

  /* -------------------------------------------------------------------- the identity ---- */

  private readonly org = snapshot(
    () => this.orgId() ?? undefined,
    (id) => this.api.loadOrg(id),
  );

  readonly isLoading = this.org.isLoading;
  readonly error = this.org.error;
  readonly isEmpty = this.org.isEmpty;
  readonly reload = () => this.org.reload();
  readonly row = computed<OrgRow | null>(() => this.org.value() ?? null);

  /* ------------------------------------------------------------------------- stores ---- */

  /** Back to the first page whenever the organization changes. */
  readonly storesPage = linkedSignal<unknown, number>({
    source: () => this.orgId(),
    computation: () => 0,
  });

  private readonly stores = snapshot(
    () => {
      const id = this.orgId();
      // Keyed on the tab, so nothing is fetched until it is opened — and not re-fetched when the
      // operator comes back to it.
      return id && this.section() === 'stores' ? {id, page: this.storesPage()} : undefined;
    },
    (query) => this.api.loadStores(query.id, query.page, PAGE_SIZE),
  );

  readonly storeRows = computed<readonly PlatformStoreRow[]>(() => this.stores.value()?.rows ?? []);
  readonly storesLoading = this.stores.isLoading;
  readonly storesError = this.stores.error;
  readonly storesTotal = computed(() => this.stores.value()?.totalElements ?? 0);
  readonly storesTotalPages = computed(() => this.stores.value()?.totalPages ?? 0);
  readonly reloadStores = () => this.stores.reload();

  /* -------------------------------------------------------------------------- users ---- */

  readonly usersPage = linkedSignal<unknown, number>({
    source: () => this.orgId(),
    computation: () => 0,
  });

  private readonly users = snapshot(
    () => {
      const id = this.orgId();
      return id && this.section() === 'users' ? {id, page: this.usersPage()} : undefined;
    },
    (query) => this.api.loadUsers(query.id, query.page, PAGE_SIZE),
  );

  readonly userRows = computed<readonly PlatformUserRow[]>(() => this.users.value()?.rows ?? []);
  readonly usersLoading = this.users.isLoading;
  readonly usersError = this.users.error;
  readonly usersTotal = computed(() => this.users.value()?.totalElements ?? 0);
  readonly usersTotalPages = computed(() => this.users.value()?.totalPages ?? 0);
  readonly reloadUsers = () => this.users.reload();

  /** What the roles dialog offers, in the reader's language. */
  readonly roleOptions = computed<readonly RoleOption[]>(() => {
    this.transloco.activeLang();
    return (this.users.value()?.assignableRoles ?? []).map((role) => ({
      value: role,
      label: this.roleLabels.label(role),
    }));
  });

  /* ------------------------------------------------------------------------ billing ---- */

  /**
   * How many rows each of the billing tab's two lists shows.
   *
   * A short list rather than a paged table: this is a tab on an organization, not the ledger. The
   * ledger is `/platform/billing` filtered to this org, and the tab links there for the rest.
   */
  readonly billingRows = BILLING_ROWS;

  private readonly billing = snapshot(
    () => {
      const id = this.orgId();
      // Keyed on the tab, so billing is not asked about until the tab is opened.
      return id && this.section() === 'billing' ? id : undefined;
    },
    (id) => this.api.loadBilling(id, BILLING_ROWS),
  );

  readonly billingLoading = this.billing.isLoading;
  readonly billingError = this.billing.error;
  readonly reloadBilling = () => this.billing.reload();

  readonly billingSubscriptions = computed(() => this.billing.value()?.subscriptions ?? []);
  readonly billingInvoices = computed(() => this.billing.value()?.invoices ?? []);
  readonly billingSubscriptionsTotal = computed(() => this.billing.value()?.subscriptionsTotal ?? 0);
  readonly billingInvoicesTotal = computed(() => this.billing.value()?.invoicesTotal ?? 0);

  /**
   * What this organization has paid and been billed, one figure per currency.
   *
   * A list rather than a total, everywhere money appears on this platform: nothing holds an exchange
   * rate, so an org trading in two currencies gets two lines rather than one invented sum.
   */
  readonly billingTotals = computed<readonly {currency: string; paid: string; due: string}[]>(() => {
    this.transloco.activeLang();
    return (this.billing.value()?.totals ?? []).map((total) => ({
      currency: total.currency.code,
      paid: this.money.account((total.paid?.minorUnits ?? 0) / MINOR_UNITS, total.currency.code),
      due: this.money.account((total.due?.minorUnits ?? 0) / MINOR_UNITS, total.currency.code),
    }));
  });

  billingAmount(value: {currency: {code: string}; minorUnits: number} | null | undefined): string {
    if (!value) {
      return '—';
    }
    return this.money.account(value.minorUnits / MINOR_UNITS, value.currency.code);
  }

  invoiceStatusLabel(status: string | null): string {
    return this.labels.invoiceStatus(status);
  }

  invoiceStatusTone(status: string | null): Tone {
    return (status && INVOICE_STATUS_TONE[status]) || 'slate';
  }

  billingDate(value: string | null | undefined): string {
    if (!value) {
      return '—';
    }
    return this.localeFormat.localizeDate(value, undefined, {dateStyle: 'medium'});
  }

  /* ---------------------------------------------------------------------- rendering ---- */

  readonly sections = ORG_SECTIONS;

  readonly heading = computed(() => {
    this.transloco.activeLang();
    const org = this.row();
    /*
     * Resolved before the `||` rather than inside it. TypeScript contextually types the right
     * operand of `||` from the left one, so `org?.label || translate(…)` infers Transloco's `T` as
     * `string | undefined` and the whole heading becomes optional — which fails only in the AOT
     * template check, several files away from the cause.
     */
    const fallback: string = this.transloco.translate('platform.organization.heading.fallback');
    return {
      title: org?.label || fallback,
      context: this.transloco.translate('platform.organization.heading.context', {
        created: org
          ? this.localeFormat.localizeDate(org.createdDate, undefined, {dateStyle: 'medium'})
          : '—',
      }),
    };
  });

  readonly statusLabel = computed(() => this.labels.orgStatus(this.row()?.status ?? null));
  readonly statusTone = computed<Tone>(() => {
    const status = this.row()?.status;
    return (status && ORG_STATUS_TONE[status]) || 'slate';
  });

  /** Whether the org is in a state each lever makes sense from. The server still decides. */
  readonly canSuspend = computed(() => this.row()?.status === 'ACTIVE');
  readonly canResume = computed(() => this.row()?.status === 'SUSPENDED');
  readonly canClose = computed(() => {
    const status = this.row()?.status;
    return status === 'ACTIVE' || status === 'SUSPENDED';
  });

  /**
   * Whether the owner password reset can be offered at all.
   *
   * False when `ownerUserId` is null, which is every organization the backfill could not resolve.
   * The button is rendered disabled with the reason rather than hidden, because "we do not know who
   * owns this" is a fact an operator needs. See lessons.md, "Organizations — the owner nobody
   * recorded".
   */
  readonly hasOwner = computed(() => !!this.row()?.ownerUserId);

  storeStatusLabel(status: string | null): string {
    return this.labels.storeStatus(status);
  }

  storeStatusTone(status: string | null): Tone {
    return (status && STORE_STATUS_TONE[status as keyof typeof STORE_STATUS_TONE]) || 'slate';
  }

  provisioningLabel(state: string | null): string {
    return this.labels.provisioningState(state);
  }

  provisioningTone(state: string | null): Tone {
    return (state && PROVISIONING_STATE_TONE[state as keyof typeof PROVISIONING_STATE_TONE]) || 'slate';
  }

  /**
   * Whether a store is paid for, as tenancy reports it on the store row.
   *
   * Free where it arrives — tenancy batch-fills it as a service principal — and coarse: it is a
   * status and nothing else. The badge is a link into `/platform/billing`'s register filtered to that
   * store, which is where the plan, the invoices, the history and the levers are. That reading was
   * impossible until billing's read guard grew a super-admin branch; see lessons.md, "Platform — a
   * store's subscription cannot be read by an operator *(answered)*".
   */
  billingLabel(status: string | null): string {
    return this.labels.subscriptionStatus(status);
  }

  /** Neutral when billing could not be reached, which is what a null means here. */
  billingTone(status: string | null): Tone {
    return (status && SUBSCRIPTION_STATUS_TONE[status as keyof typeof SUBSCRIPTION_STATUS_TONE]) || 'slate';
  }

  roleList(roles: readonly string[]): string {
    return this.roleLabels.labels(roles);
  }

  /* ------------------------------------------------------------------------- writes ---- */

  startRename(): void {
    this.renaming.set(true);
    this.draftName.set(this.row()?.name ?? '');
  }

  cancelRename(): void {
    this.renaming.set(false);
  }

  /** Renames, then re-reads: the header shows what tenancy stored, not what was typed. */
  saveName(): void {
    const id = this.orgId();
    const name = this.draftName().trim();
    if (!id || !name || this.busy()) {
      return;
    }
    this.run(this.api.rename(id, name), 'platform.organization.toast.renamed', {name}, () => {
      this.renaming.set(false);
      this.org.reload();
    });
  }

  ask(prompt: OrgPrompt): void {
    this.suspendReason.set('');
    this.prompt.set(prompt);
  }

  dismissPrompt(): void {
    this.prompt.set(null);
  }

  /**
   * Carries out whichever lifecycle move is being confirmed.
   *
   * An illegal move comes back as `IllegalLifecycleTransitionException` and is shown as the server's
   * own refusal. The console does not reproduce `OrgLifecycleService`'s transition table.
   */
  confirmPrompt(): void {
    const id = this.orgId();
    const prompt = this.prompt();
    if (!id || !prompt || this.busy()) {
      return;
    }
    const name = this.row()?.label ?? '';
    const request =
      prompt === 'suspend'
        ? this.api.suspend(id, this.suspendReason().trim())
        : prompt === 'resume'
          ? this.api.resume(id)
          : this.api.close(id);

    this.run(request, LIFECYCLE_TOAST[prompt], {name}, () => {
      this.prompt.set(null);
      this.org.reload();
    });
  }

  startOwnerReset(): void {
    this.resettingOwner.set(true);
  }

  dismissOwnerReset(): void {
    this.resettingOwner.set(false);
  }

  /**
   * Sets the organization owner's password.
   *
   * Keyed on the **organization** id: tenancy resolves the owner from it. There is no
   * current-password field because nothing verifies one, and nothing emails the new password
   * because nothing on the platform sends mail — the dialog says both.
   */
  confirmOwnerReset(password: string): void {
    const id = this.orgId();
    if (!id || this.busy()) {
      return;
    }
    this.run(
      this.api.changeOwnerPassword(id, password),
      'platform.organization.toast.ownerPasswordReset',
      {name: this.row()?.label ?? ''},
      () => this.resettingOwner.set(false),
    );
  }

  /* -------------------------------------------------------------- the users tab's writes ---- */

  askResetUser(row: PlatformUserRow): void {
    this.resettingUser.set(row);
  }

  askEditRoles(row: PlatformUserRow): void {
    this.editingRoles.set(row);
  }

  askDeleteUser(row: PlatformUserRow): void {
    this.deletingUser.set(row);
  }

  dismissUserDialogs(): void {
    this.resettingUser.set(null);
    this.editingRoles.set(null);
    this.deletingUser.set(null);
  }

  /** Enable or disable. Re-reads rather than flipping the row, so the tab shows uaa's answer. */
  toggleUserEnabled(row: PlatformUserRow): void {
    this.applyToUser(
      row,
      {kind: row.enabled ? 'disable' : 'enable'},
      row.enabled ? 'platform.users.toast.disabled' : 'platform.users.toast.enabled',
    );
  }

  confirmUserReset(password: string): void {
    const row = this.resettingUser();
    if (row) {
      this.applyToUser(row, {kind: 'resetPassword', password}, 'platform.users.toast.passwordReset');
    }
  }

  confirmRoles(change: RoleChange): void {
    const row = this.editingRoles();
    if (row) {
      this.applyToUser(row, {kind: 'setRoles', ...change}, 'platform.users.toast.rolesChanged');
    }
  }

  confirmDeleteUser(): void {
    const row = this.deletingUser();
    if (row) {
      this.applyToUser(row, {kind: 'delete'}, 'platform.users.toast.deleted');
    }
  }

  /**
   * One account action.
   *
   * `SuperAdminImmutableException` is what comes back when the target is the platform's own super
   * admin; it is surfaced as the server's refusal rather than predicted, because which account that
   * is belongs to uaa's configuration.
   */
  private applyToUser(row: PlatformUserRow, action: AdminUserAction, toastKey: string): void {
    if (this.busy()) {
      return;
    }
    this.run(this.api.applyToUser(row.id, action), toastKey, {name: row.username}, () => {
      this.dismissUserDialogs();
      this.users.reload();
    });
  }

  /** One shape for every write on this page: lock, report, re-read, or surface the refusal. */
  private run(
    request: ReturnType<OrganizationDetailApi['resume']>,
    toastKey: string,
    params: Record<string, string>,
    done: () => void,
  ): void {
    this.busy.set(true);
    request.subscribe({
      next: () => {
        this.busy.set(false);
        this.toast.success(this.transloco.translate(toastKey, params));
        done();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.toast.danger(this.apiErrors.messageFor(failure));
      },
    });
  }
}
