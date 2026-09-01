import {Component, computed, effect, inject, input, untracked} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import type {PodRow} from '@models/platform';
import {Badge, BusyOverlay, CopyField, DataTable, type TableColumn, TableRow, EmptyState, Icon, LoadError, NoticeBar, PageHeader, Pagination, Panel, SearchBox} from '@cvhome-saas/ui-kit/ui';
import {PAGE_SIZE, PodsFacade} from './facades/pods.facade';

/** The table's columns. Widths are grid tracks, read straight into the row layout. */
const COLUMN_KEYS: readonly {key: string; labelKey: string; width: string}[] = [
  {key: 'pod', labelKey: 'platform.pods.column.pod', width: 'minmax(11rem, 1.6fr)'},
  {key: 'endpoint', labelKey: 'platform.pods.column.endpoint', width: 'minmax(13rem, 2.2fr)'},
  {key: 'owner', labelKey: 'platform.pods.column.owner', width: 'minmax(9rem, 1.2fr)'},
  {key: 'stores', labelKey: 'platform.pods.column.stores', width: 'minmax(5rem, 0.6fr)'},
  {key: 'shortId', labelKey: 'platform.pods.column.shortId', width: 'minmax(9rem, 1fr)'},
  {key: 'actions', labelKey: '', width: '3.5rem'},
];

/**
 * The fleet: every pod on the platform.
 *
 * **No lifecycle, health or capacity columns.** The paged endpoint answers the routing `Pod`;
 * everything operational is on `PodView`, which is reachable only per id, so a column here would
 * cost one request per row. They are on the detail page, which fetches exactly one.
 *
 * The store count is the exception, and only because tenancy will answer every pod's at once. It
 * comes from `manager_store.pod_id` rather than the registry's own `capacity_stores`, which is a
 * mirror and can lag — see lessons.md, "Pods — the registry's store count is a mirror, and mirrors
 * drift".
 */
@Component({
  selector: 'app-pods',
  imports: [
    Badge,
    BusyOverlay,
    CopyField,
    DataTable,
    EmptyState,
    Icon,
    LoadError,
    NoticeBar,
    PageHeader,
    Pagination,
    Panel,
    RouterLink,
    SearchBox,
    TableRow,
    TranslocoDirective,
  ],
  providers: [PodsFacade],
  templateUrl: './pods.html',
  styleUrl: './pods.css',
})
export class Pods {
  private readonly transloco = inject(TranslocoService);
  private readonly router = inject(Router);

  protected readonly facade = inject(PodsFacade);

  /** The page being read, from `?page=`, so a page survives a reload and can be linked to. */
  readonly page = input<string>();

  /** The search term, from `?q=`, for the same reason. */
  readonly q = input<string>();

  protected readonly isLoading = this.facade.isLoading;
  protected readonly isEmpty = this.facade.isEmpty;
  protected readonly error = this.facade.error;
  protected readonly rows = this.facade.rows;
  protected readonly heading = this.facade.heading;
  protected readonly pageSize = PAGE_SIZE;

  /* Bound once as fields: a method reference created in a binding is a new function every tick. */
  protected readonly ownerLabel = (orgId: string | null) => this.facade.ownerLabel(orgId);
  protected readonly storeCount = (stores: number | null) => this.facade.storeCount(stores);

  protected readonly columns = computed<readonly TableColumn[]>(() => {
    this.transloco.activeLang();
    return COLUMN_KEYS.map((column) => ({
      key: column.key,
      label: column.labelKey ? this.transloco.translate(column.labelKey) : '',
      width: column.width,
    }));
  });

  constructor() {
    /*
     * The URL is what a reload and a shared link restore this page's state from; the handlers below
     * are the other writer, so these only carry what the page did not cause. Each reads its own
     * signal untracked, or the effect woken by a handler's write would see the new value beside a
     * URL that had not caught up and put the old one straight back.
     */
    effect(() => {
      const requested = Number(this.page() ?? 0);
      const index = Number.isFinite(requested) && requested > 0 ? Math.floor(requested) : 0;
      if (index !== untracked(() => this.facade.pageIndex())) {
        this.facade.goToPage(index);
      }
    });

    effect(() => {
      const term = this.q() ?? '';
      if (term !== untracked(() => this.facade.search())) {
        this.facade.setSearch(term);
      }
    });
  }

  protected onPage(page: number): void {
    this.facade.goToPage(page);
    void this.router.navigate([], {queryParams: {page: page || null}, queryParamsHandling: 'merge'});
  }

  /** Searching resets the page, in the URL as well as in the facade. */
  protected onSearch(term: string): void {
    this.facade.setSearch(term);
    void this.router.navigate([], {queryParams: {q: term || null, page: null}, queryParamsHandling: 'merge'});
  }

  protected open(row: PodRow): void {
    void this.router.navigate(['/platform/pods', row.id]);
  }
}
