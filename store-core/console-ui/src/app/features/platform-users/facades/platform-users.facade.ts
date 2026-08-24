import {Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import type {AdminUserAction} from '@api/uaa/admin-user.service';
import {ApiErrorService} from '@core/errors/api-error.service';
import type {PlatformUserRow} from '@models/platform';
import {RoleLabel} from '@shared/i18n/role-label';
import {snapshot} from '@shared/state/snapshot';
import type {RoleChange, RoleOption} from '@shared/ui/roles-dialog/roles-dialog';
import type {SelectOption} from '@shared/ui/select/select';
import {ToastService} from '@shared/ui/toast/toast';
import {PlatformUsersApi} from '../services/platform-users.api.service';

export const PAGE_SIZE = 20;

/**
 * Every account on the platform, and what a super admin may do to one.
 *
 * The counterpart to Module 8's team page, and a different question: that one asks tenancy for the
 * open *store's* people and therefore cannot see an org admin at all, because an org admin has no
 * `metadata.store`. This asks uaa directly.
 *
 * **`SuperAdminImmutableException` is not predicted.** uaa refuses to disable or delete the
 * platform's own super admin, and which account that is belongs to uaa's configuration — so the
 * console offers the action and renders the server's refusal, rather than hiding a control based on
 * a guess about someone else's config.
 */
@Injectable()
export class PlatformUsersFacade {
  private readonly api = inject(PlatformUsersApi);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly roleLabels = inject(RoleLabel);

  readonly busy = signal(false);

  /** The organization filter, or `''` for everyone. The only filter uaa's list understands. */
  readonly orgFilter = signal('');

  /** Back to the first page whenever the filter changes — page 4 of a smaller result is nothing. */
  readonly pageIndex = linkedSignal<unknown, number>({
    source: () => this.orgFilter(),
    computation: () => 0,
  });

  /** The account a dialog is acting on, or null. */
  readonly resetting = signal<PlatformUserRow | null>(null);
  readonly editingRoles = signal<PlatformUserRow | null>(null);
  readonly deleting = signal<PlatformUserRow | null>(null);

  private readonly users = snapshot(
    () => ({page: this.pageIndex(), count: PAGE_SIZE, org: this.orgFilter()}),
    (query) => this.api.load(query),
  );

  readonly isLoading = this.users.isLoading;
  readonly error = this.users.error;
  readonly isEmpty = this.users.isEmpty;
  readonly reload = () => this.users.reload();

  readonly rows = computed<readonly PlatformUserRow[]>(() => this.users.value()?.rows ?? []);
  readonly totalElements = computed(() => this.users.value()?.totalElements ?? 0);
  readonly totalPages = computed(() => this.users.value()?.totalPages ?? 0);

  readonly heading = computed(() => {
    this.transloco.activeLang();
    return {
      title: this.transloco.translate('platform.users.heading.title'),
      context: this.transloco.translate('platform.users.heading.context'),
    };
  });

  readonly totalLabel = computed<string | null>(() => {
    if (this.isEmpty()) {
      return null;
    }
    this.transloco.activeLang();
    return this.transloco.translate('platform.users.totalLabel', {
      total: this.localeFormat.localizeNumber(this.totalElements(), 'decimal'),
      count: this.totalElements(),
    });
  });

  readonly subtitle = computed(() => {
    this.transloco.activeLang();
    const shown = this.rows().length;
    if (!shown) {
      return this.transloco.translate('platform.users.subtitle.none');
    }
    const digits = (value: number) => this.localeFormat.localizeNumber(value, 'decimal');
    const from = this.pageIndex() * PAGE_SIZE + 1;
    return this.transloco.translate('platform.users.subtitle.range', {
      from: digits(from),
      to: digits(from + shown - 1),
      total: digits(this.totalElements()),
      count: this.totalElements(),
    });
  });

  /** The filter's options. The empty value is every account, which is the page's resting state. */
  readonly orgOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return [
      {value: '', label: this.transloco.translate('platform.users.filter.allOrgs')},
      ...(this.users.value()?.orgs ?? []).map((org) => ({value: org.id, label: org.label})),
    ];
  });

  readonly roleOptions = computed<readonly RoleOption[]>(() => {
    this.transloco.activeLang();
    return (this.users.value()?.assignableRoles ?? []).map((role) => ({
      value: role,
      label: this.roleLabels.label(role),
    }));
  });

  /** An organization id, named where the lookup reached it. Falls back to the id. */
  orgLabel(orgId: string | null): string {
    if (!orgId) {
      return '';
    }
    return this.users.value()?.orgNames.get(orgId) ?? orgId;
  }

  roleList(roles: readonly string[]): string {
    return this.roleLabels.labels(roles);
  }

  setOrgFilter(orgId: string): void {
    this.orgFilter.set(orgId);
  }

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }

  /* ------------------------------------------------------------------------- writes ---- */

  askReset(row: PlatformUserRow): void {
    this.resetting.set(row);
  }

  askEditRoles(row: PlatformUserRow): void {
    this.editingRoles.set(row);
  }

  askDelete(row: PlatformUserRow): void {
    this.deleting.set(row);
  }

  dismissDialogs(): void {
    this.resetting.set(null);
    this.editingRoles.set(null);
    this.deleting.set(null);
  }

  /** Enable or disable. Re-reads rather than flipping the row, so the table shows uaa's answer. */
  toggleEnabled(row: PlatformUserRow): void {
    this.apply(
      row,
      {kind: row.enabled ? 'disable' : 'enable'},
      row.enabled ? 'platform.users.toast.disabled' : 'platform.users.toast.enabled',
    );
  }

  confirmReset(password: string): void {
    const row = this.resetting();
    if (row) {
      this.apply(row, {kind: 'resetPassword', password}, 'platform.users.toast.passwordReset');
    }
  }

  confirmRoles(change: RoleChange): void {
    const row = this.editingRoles();
    if (row) {
      this.apply(row, {kind: 'setRoles', ...change}, 'platform.users.toast.rolesChanged');
    }
  }

  confirmDelete(): void {
    const row = this.deleting();
    if (row) {
      this.apply(row, {kind: 'delete'}, 'platform.users.toast.deleted');
    }
  }

  private apply(row: PlatformUserRow, action: AdminUserAction, toastKey: string): void {
    if (this.busy()) {
      return;
    }
    this.busy.set(true);
    this.api.apply(row.id, action).subscribe({
      next: () => {
        this.busy.set(false);
        this.dismissDialogs();
        this.toast.success(this.transloco.translate(toastKey, {name: row.username}));
        this.users.reload();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        // `SuperAdminImmutableException` lands here, and reads as uaa's own sentence about why not.
        this.toast.danger(this.apiErrors.messageFor(failure));
      },
    });
  }
}
