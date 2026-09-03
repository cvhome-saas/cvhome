import {Injectable, computed, inject, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {ApiErrorService, snapshot, type KpiDatum} from '@cvhome-saas/ui-kit';
import {ToastService, type TabItem} from '@cvhome-saas/ui-kit/ui';
import {AdminClientService, type ClientSummary, type ClientType} from '@cvhome-saas/ui-kit/uaa';

export const PAGE_SIZE = 20;

/** The segments above the table. `ALL` is no filter; the rest map onto the server's `enabled` and `type`. */
export type ClientFilter = 'ALL' | 'ENABLED' | 'DISABLED' | 'MACHINE' | 'CONFIDENTIAL' | 'PUBLIC';
export const CLIENT_FILTERS: readonly ClientFilter[] = ['ALL', 'ENABLED', 'DISABLED', 'MACHINE', 'CONFIDENTIAL', 'PUBLIC'];

/**
 * The client registry, as a list.
 *
 * **The row carries what the table draws.** `ClientSummary` has the type (derived on the server from
 * how the client authenticates), whether it is enabled, its grant types, the secret's expiry and the
 * last token — so the five columns the design draws no longer need a read per row. Search and the
 * segments are the server's `q`, `enabled` and `type` filters. See lessons.md, "Clients — the list
 * carries three fields".
 *
 * **Enable and disable are row actions.** Disabling revokes every token the client holds, which is
 * the reason to do it; the toggle says so in its toast. The tiles come from `/stats` and are re-read
 * after every write.
 *
 * Editing is a route, not a pane: `ClientDetails` has five groups of settings and two open maps
 * behind it, which is a page. Everything about one client lives in `@features/client-form`.
 */
@Injectable()
export class ClientsFacade {
  private readonly clients = inject(AdminClientService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  readonly pageIndex = signal(0);
  readonly query = signal('');
  readonly filter = signal<ClientFilter>('ALL');
  readonly busy = signal(false);

  private readonly page = snapshot(
    () => ({page: this.pageIndex(), count: PAGE_SIZE, q: this.query(), filter: this.filter()}),
    (query) => this.clients.list(query.page, query.count, {q: query.q, ...searchOf(query.filter)}),
  );

  private readonly stats = snapshot(
    () => ({}),
    () => this.clients.stats(),
  );

  readonly isLoading = this.page.isLoading;
  readonly error = this.page.error;
  readonly isEmpty = this.page.isEmpty;
  readonly reload = () => this.refreshAll();

  readonly rows = computed(() => this.page.value()?.content ?? []);
  readonly totalElements = computed(() => this.page.value()?.totalElements ?? 0);
  readonly totalPages = computed(() => this.page.value()?.totalPages ?? 0);

  /** The tiles: registered with the enabled share, secrets expiring within thirty days, machine, public. */
  readonly tiles = computed<readonly KpiDatum[]>(() => {
    this.transloco.activeLang();
    const s = this.stats.value();
    const t = (key: string, params?: Record<string, unknown>) => this.transloco.translate(`clients.tiles.${key}`, params);
    if (!s) {
      return [];
    }
    return [
      {label: t('total'), value: String(s.total), icon: 'code', tone: 'blue', flag: t('enabledOf', {count: s.enabled})},
      {
        label: t('expiring'),
        value: String(s.secretsExpiringSoon),
        icon: 'clock',
        tone: s.secretsExpiringSoon ? 'amber' : 'slate',
        flag: t('expiringHint'),
      },
      {label: t('machine'), value: String(s.machine), icon: 'server', tone: 'cyan', flag: t('machineHint')},
      {label: t('publicClients'), value: String(s.publicClients), icon: 'globe', tone: 'slate', flag: t('publicHint')},
    ];
  });

  readonly filterTabs = computed<readonly TabItem[]>(() => {
    this.transloco.activeLang();
    const s = this.stats.value();
    const counts: Partial<Record<ClientFilter, number | undefined>> = {
      ALL: s?.total,
      ENABLED: s?.enabled,
      DISABLED: s?.disabled,
      MACHINE: s?.machine,
      CONFIDENTIAL: s?.confidential,
      PUBLIC: s?.publicClients,
    };
    return CLIENT_FILTERS.map((key) => ({
      key,
      label: this.transloco.translate(`clients.filter.${key}`),
      badge: counts[key] === undefined ? undefined : String(counts[key]),
      badgeTone: key === 'DISABLED' && counts[key] ? 'amber' : 'slate',
    }));
  });

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }

  setQuery(value: string): void {
    this.query.set(value);
    this.pageIndex.set(0);
  }

  setFilter(value: string): void {
    this.filter.set(value as ClientFilter);
    this.pageIndex.set(0);
  }

  /** Disabling revokes the client's tokens as well; the toast says which happened. */
  toggleEnabled(row: ClientSummary): void {
    this.busy.set(true);
    const call = row.enabled ? this.clients.disable(row.id) : this.clients.enable(row.id);
    call.subscribe({
      next: () => {
        this.busy.set(false);
        this.toast.success(
          this.transloco.translate(row.enabled ? 'clients.toast.disabled' : 'clients.toast.enabled', {clientId: row.clientId}),
        );
        this.refreshAll();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  private refreshAll(): void {
    this.page.reload();
    this.stats.reload();
  }
}

/** The segment as the server's two filters. */
function searchOf(filter: ClientFilter): {enabled?: boolean; type?: ClientType} {
  switch (filter) {
    case 'ENABLED':
      return {enabled: true};
    case 'DISABLED':
      return {enabled: false};
    case 'MACHINE':
    case 'CONFIDENTIAL':
    case 'PUBLIC':
      return {type: filter};
    default:
      return {};
  }
}
