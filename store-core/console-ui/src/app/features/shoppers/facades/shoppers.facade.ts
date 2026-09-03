import {Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {ApiErrorService, snapshot} from '@cvhome-saas/ui-kit';
import type {PlatformUserRow, SessionSummary, UserStatus} from '@cvhome-saas/ui-kit/uaa';
import type {SelectOption} from '@cvhome-saas/ui-kit/ui';
import {ToastService} from '@cvhome-saas/ui-kit/ui';
import {ShoppersApi} from '../services/shoppers.api.service';

export const PAGE_SIZE = 20;

/**
 * This store's shoppers, and what its merchant may do to one.
 *
 * The platform's account screen is the counterpart, and the difference is not cosmetic: that one
 * administers staff, this one administers the store's customers. A merchant may find an account,
 * see where it is signed in, unlock it, sign it out, disable it and remove it — and nothing else,
 * because nothing else is theirs to do.
 */
@Injectable()
export class ShoppersFacade {
  private readonly api = inject(ShoppersApi);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);

  readonly busy = signal(false);

  readonly query = signal('');
  readonly status = signal<UserStatus | ''>('');

  /** Back to the first page whenever a filter changes — page 4 of a smaller result is nothing. */
  readonly pageIndex = linkedSignal<unknown, number>({
    source: () => [this.query(), this.status()],
    computation: () => 0,
  });

  /** The account a dialog is acting on, or null. */
  readonly deleting = signal<PlatformUserRow | null>(null);
  readonly inspecting = signal<PlatformUserRow | null>(null);
  readonly sessions = signal<readonly SessionSummary[]>([]);
  readonly sessionsFailed = signal(false);

  private readonly shoppers = snapshot(
    () => ({page: this.pageIndex(), count: PAGE_SIZE, q: this.query(), status: this.status()}),
    (query) => this.api.load(query),
  );

  readonly isLoading = this.shoppers.isLoading;
  readonly error = this.shoppers.error;
  readonly isEmpty = this.shoppers.isEmpty;
  readonly reload = () => this.shoppers.reload();

  readonly rows = computed<readonly PlatformUserRow[]>(() => this.shoppers.value()?.rows ?? []);
  readonly totalElements = computed(() => this.shoppers.value()?.totalElements ?? 0);
  readonly totalPages = computed(() => this.shoppers.value()?.totalPages ?? 0);

  /** True when a filter is on, so an empty result reads as "no matches" rather than "no shoppers". */
  readonly filtered = computed(() => !!this.query().trim() || !!this.status());

  readonly totalLabel = computed<string | null>(() => {
    if (this.isEmpty()) {
      return null;
    }
    this.transloco.activeLang();
    return this.transloco.translate('shoppers.totalLabel', {
      total: this.localeFormat.localizeNumber(this.totalElements(), 'decimal'),
    });
  });

  readonly statusOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return [
      {value: '', label: this.transloco.translate('shoppers.filter.anyStatus')},
      {value: 'ACTIVE', label: this.transloco.translate('shoppers.status.active')},
      {value: 'LOCKED', label: this.transloco.translate('shoppers.status.locked')},
      {value: 'DISABLED', label: this.transloco.translate('shoppers.status.disabled')},
    ];
  });

  setQuery(value: string): void {
    this.query.set(value);
  }

  setStatus(value: string): void {
    this.status.set(value as UserStatus | '');
  }

  setPage(page: number): void {
    this.pageIndex.set(page);
  }

  toggleEnabled(row: PlatformUserRow): void {
    const action = row.enabled ? this.api.disable(row.id) : this.api.enable(row.id);
    this.run(action, row.enabled ? 'shoppers.toast.disabled' : 'shoppers.toast.enabled', row);
  }

  unlock(row: PlatformUserRow): void {
    this.run(this.api.unlock(row.id), 'shoppers.toast.unlocked', row);
  }

  /**
   * Opens the sessions pane and loads it.
   *
   * A failure is shown inside the pane rather than as a toast over a closed dialog: the merchant
   * asked to see something, and an empty list that means "could not load" is a lie.
   */
  inspect(row: PlatformUserRow): void {
    this.inspecting.set(row);
    this.sessions.set([]);
    this.sessionsFailed.set(false);
    this.busy.set(true);
    this.api.sessions(row.id).subscribe({
      next: (sessions) => {
        this.sessions.set(sessions);
        this.busy.set(false);
      },
      error: () => {
        this.sessionsFailed.set(true);
        this.busy.set(false);
      },
    });
  }

  signOutEverywhere(): void {
    const row = this.inspecting();
    if (!row) {
      return;
    }
    this.busy.set(true);
    this.api.revokeSessions(row.id).subscribe({
      next: (result) => {
        this.busy.set(false);
        this.sessions.set([]);
        this.toast.success(this.transloco.translate('shoppers.toast.signedOut', {count: result.revoked}));
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.toast.danger(this.apiErrors.messageFor(failure));
      },
    });
  }

  confirmDelete(): void {
    const row = this.deleting();
    if (!row) {
      return;
    }
    this.run(this.api.delete(row.id), 'shoppers.toast.deleted', row);
  }

  dismissDialogs(): void {
    this.deleting.set(null);
    this.inspecting.set(null);
    this.sessions.set([]);
    this.sessionsFailed.set(false);
  }

  private run(action: ReturnType<ShoppersApi['unlock']>, messageKey: string, row: PlatformUserRow): void {
    this.busy.set(true);
    action.subscribe({
      next: () => {
        this.busy.set(false);
        this.dismissDialogs();
        this.toast.success(this.transloco.translate(messageKey, {name: row.username}));
        this.reload();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.toast.danger(this.apiErrors.messageFor(failure));
      },
    });
  }
}
