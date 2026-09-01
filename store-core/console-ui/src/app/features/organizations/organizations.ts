import {Component, computed, effect, inject, input, untracked} from '@angular/core';
import {Router} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {TranslocoDatePipe} from '@jsverse/transloco-locale';

import type {OrgRow} from '@models/platform';
import {Badge, BusyOverlay, DataTable, type TableColumn, TableRow, EmptyState, Icon, LoadError, PageHeader, Pagination, Panel, SearchBox, Select} from '@cvhome-saas/ui-kit/ui';
import {CreateOrgDialog} from './components/create-org-dialog/create-org-dialog';
import {OrganizationsFacade, PAGE_SIZE} from './facades/organizations.facade';

/** The table's columns. Widths are grid tracks, read straight into the row layout. */
const COLUMN_KEYS: readonly {key: string; labelKey: string; width: string}[] = [
  {key: 'organization', labelKey: 'platform.organizations.column.organization', width: 'minmax(14rem, 2.4fr)'},
  {key: 'status', labelKey: 'platform.organizations.column.status', width: 'minmax(6rem, 0.7fr)'},
  {key: 'created', labelKey: 'platform.organizations.column.created', width: 'minmax(8rem, 1fr)'},
  {key: 'id', labelKey: 'platform.organizations.column.id', width: 'minmax(8rem, 1fr)'},
  {key: 'actions', labelKey: '', width: '3.5rem'},
];

/**
 * Every organization on the platform.
 *
 * The tenant registry, and the first screen in this console that is not a reading of one shop.
 *
 * Searchable by name or contact email and filterable by status, both **server-side** — the term goes
 * to `POST org-manager/list`, so it narrows the whole registry rather than the twenty rows on screen.
 *
 * **What it still does not have:** a store count, a user count or a plan on the row. Each would be
 * one more paged call per organization, so they are a lessons entry rather than columns filled with
 * something plausible.
 */
@Component({
  selector: 'app-organizations',
  imports: [
    Badge,
    BusyOverlay,
    CreateOrgDialog,
    DataTable,
    EmptyState,
    Icon,
    LoadError,
    PageHeader,
    Pagination,
    Panel,
    SearchBox,
    Select,
    TableRow,
    TranslocoDatePipe,
    TranslocoDirective,
  ],
  providers: [OrganizationsFacade],
  templateUrl: './organizations.html',
  styleUrl: './organizations.css',
})
export class Organizations {
  private readonly transloco = inject(TranslocoService);
  private readonly router = inject(Router);

  protected readonly facade = inject(OrganizationsFacade);

  /**
   * The page being read, from `?page=`.
   *
   * In the URL rather than in a signal alone so a page an operator is on survives a reload and can
   * be linked to — the page contract, which the two filters below follow for the same reason.
   */
  readonly page = input<string>();

  /** The search term, from `?q=`. */
  readonly q = input<string>();

  /** The status filter, from `?status=`. */
  readonly status = input<string>();

  protected readonly isLoading = this.facade.isLoading;
  protected readonly isEmpty = this.facade.isEmpty;
  protected readonly error = this.facade.error;
  protected readonly rows = this.facade.rows;
  protected readonly heading = this.facade.heading;
  protected readonly pageSize = PAGE_SIZE;

  /*
   * Bound once as fields rather than as `facade.x.bind(facade)` in the template: a method reference
   * created in a binding is a new function every change detection.
   */
  protected readonly statusLabel = (status: string | null) => this.facade.statusLabel(status);
  protected readonly statusTone = (status: string | null) => this.facade.statusTone(status);

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
     * The URL is what a reload and a shared link restore this page's state from, and the `on*`
     * handlers below are the other writer — so these effects only carry the cases the page did not
     * cause: a first render, a back button, a pasted link.
     *
     * **Each reads its own signal untracked, and that is load-bearing.** The handler sets the state
     * and *then* navigates, so an effect woken by its own subject would see the new value beside a
     * URL that had not caught up and would immediately put the old one back. Module 9 shipped that
     * bug on the customers page and the fix is the same: the state leads, this only follows.
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

    effect(() => {
      const status = this.status() ?? '';
      if (status !== untracked(() => this.facade.status())) {
        this.facade.setStatus(status);
      }
    });
  }

  /** The state leads and the URL mirrors it — a table that waited for the navigation would lag. */
  protected onPage(page: number): void {
    this.facade.goToPage(page);
    void this.router.navigate([], {queryParams: {page: page || null}, queryParamsHandling: 'merge'});
  }

  /** Narrowing resets the page, in the URL as well as in the facade — page 4 of a smaller result is nothing. */
  protected onSearch(term: string): void {
    this.facade.setSearch(term);
    void this.router.navigate([], {queryParams: {q: term || null, page: null}, queryParamsHandling: 'merge'});
  }

  protected onStatus(status: string): void {
    this.facade.setStatus(status);
    void this.router.navigate([], {queryParams: {status: status || null, page: null}, queryParamsHandling: 'merge'});
  }

  protected open(row: OrgRow): void {
    void this.router.navigate(['/platform/organizations', row.id]);
  }
}
