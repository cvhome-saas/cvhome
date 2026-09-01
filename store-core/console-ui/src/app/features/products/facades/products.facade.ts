import {Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {ApiErrorService} from '@cvhome-saas/ui-kit';
import type {PageT} from '@cvhome-saas/ui-kit';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import {
  NO_FILTERS,
  PRODUCT_TABS,
  type InlineProductEdit,
  type ProductFilterOption,
  type ProductFilters,
  type ProductRow,
  type ProductTab,
  type ProductsSnapshot,
} from '@models/products';
import type {TabItem} from '@cvhome-saas/ui-kit/ui';
import {ToastService} from '@cvhome-saas/ui-kit/ui';
import {ProductsApi, type ProductsQuery} from '../services/products.api.service';
import {ProductsCache} from '@api/catalog/products-cache';

export const PAGE_SIZE = 20;

const EMPTY_PAGE: PageT<ProductRow> = {
  size: PAGE_SIZE,
  totalElements: 0,
  totalPages: 0,
  pageNumber: 0,
  content: [],
};

/** A product queued for deletion, waiting on the confirm dialog. */
interface PendingDelete {
  readonly id: number;
  readonly name: string;
}

/**
 * The product list's data.
 *
 * Follows `OrdersFacade` exactly: filters and a page as signals, an `rxResource` keyed on all of
 * them, a `linkedSignal` last-good snapshot so the table does not blank between requests, and
 * `isLoading` / `error` / `retry()` with the same meanings.
 *
 * **The tabs are availability, not stock.** The design's four — In stock, Low, Out of stock,
 * Overstock — need a reorder point and a stock sum the platform does not have. `ProductCriteria`
 * offers `available`, which is a real boolean on the product, so that is what the strip filters on.
 * See lessons.md.
 */
@Injectable()
export class ProductsFacade {
  private readonly api = inject(ProductsApi);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly cache = inject(ProductsCache);

  readonly activeTab = signal<ProductTab>('all');
  readonly filters = signal<ProductFilters>(NO_FILTERS);

  /**
   * The page being read.
   *
   * A `linkedSignal` over the filters, so narrowing the list drops the reader back to page one —
   * holding the index would ask for page four of a two-page result.
   */
  readonly pageIndex = linkedSignal<unknown, number>({
    source: () => [this.activeTab(), this.filters(), this.shell.currentStoreId()],
    computation: () => 0,
  });

  /** Idle until the store is known, so the page fires one request rather than two. */
  private readonly query = computed<ProductsQuery | undefined>(() => {
    const storeId = this.shell.currentStoreId();
    if (!storeId) {
      return undefined;
    }
    return {
      tab: this.activeTab(),
      filters: this.filters(),
      page: {page: this.pageIndex(), count: PAGE_SIZE},
    };
  });

  /**
   * Whether the list is on screen.
   *
   * The facade is root-provided, so anything that injects it constructs it — and an `rxResource`
   * starts as soon as it exists. The product form injected this facade for one method and paid for a
   * page of products it never displayed. The page sets this in its constructor; nothing else does.
   */
  readonly active = signal(false);

  private readonly snapshot = rxResource({
    params: () => {
      // The store id is part of the key though no argument is built from it: the request context
      // stamps `?store=` itself, and without it here the table would keep one store's products
      // under another store's name.
      this.shell.currentStoreId();
      // A write elsewhere in the app invalidates the list by bumping this.
      this.cache.stamp();
      return this.active() ? this.query() : undefined;
    },
    stream: ({params}) => this.api.loadSnapshot(params),
  });

  private readonly loaded = linkedSignal<ProductsSnapshot | undefined, ProductsSnapshot | undefined>({
    source: () => (this.snapshot.hasValue() ? this.snapshot.value() : undefined),
    computation: (incoming, previous) => incoming ?? previous?.value,
  });

  readonly isLoading = this.snapshot.isLoading;
  readonly error = computed(() => this.snapshot.error() as Error | undefined);
  readonly isEmpty = computed(() => this.loaded() === undefined);
  readonly busy = signal(false);

  readonly page = computed<PageT<ProductRow>>(() => this.loaded()?.page ?? EMPTY_PAGE);
  readonly products = computed<readonly ProductRow[]>(() => this.page().content);
  readonly categories = computed<readonly ProductFilterOption[]>(() => this.loaded()?.categories ?? []);
  readonly brands = computed<readonly ProductFilterOption[]>(() => this.loaded()?.brands ?? []);
  readonly currency = computed(() => this.loaded()?.currency ?? null);

  readonly heading = computed(() => {
    this.transloco.activeLang();
    return {
      title: this.transloco.translate('products.heading.title'),
      context: this.transloco.translate('products.heading.context', {
        store: this.shell.currentStore()?.name ?? '',
      }),
    };
  });

  /**
   * The line under the title.
   *
   * The total SKU count lives here — the one real figure the design's four-tile KPI row had, and
   * the reason that row could be removed rather than filled with em dashes.
   */
  readonly context = computed(() => {
    this.transloco.activeLang();
    const total = this.loaded()?.page.totalElements;
    return total === undefined
      ? this.heading().context
      : this.transloco.translate('products.heading.contextWithCount', {
          store: this.shell.currentStore()?.name ?? '',
          count: total,
        });
  });

  readonly tabs = computed<readonly TabItem[]>(() => {
    this.transloco.activeLang();
    // No badges: a per-tab count would be one extra list request per tab, for a number the
    // pagination footer already gives for the tab actually being looked at.
    return PRODUCT_TABS.map((tab) => ({
      key: tab,
      label: this.transloco.translate(`products.tab.${tab}`),
    }));
  });

  readonly subtitle = computed(() => {
    this.transloco.activeLang();
    const page = this.page();
    if (!page.content.length) {
      return this.transloco.translate('products.subtitle.none');
    }
    const digits = (value: number) => this.localeFormat.localizeNumber(value, 'decimal');
    const from = page.pageNumber * (page.size || PAGE_SIZE) + 1;
    return this.transloco.translate('products.subtitle.range', {
      from: digits(from),
      to: digits(from + page.content.length - 1),
      total: digits(page.totalElements),
      count: page.totalElements,
    });
  });

  /** True when what is on screen is narrowed by something the operator can undo. */
  readonly filtered = computed(() => {
    const {sku, categoryId, brandId} = this.filters();
    return this.activeTab() !== 'all' || sku.trim() !== '' || categoryId !== null || brandId !== null;
  });

  setFilter<K extends keyof ProductFilters>(key: K, value: ProductFilters[K]): void {
    this.filters.update((filters) => ({...filters, [key]: value}));
  }

  clearFilters(): void {
    this.activeTab.set('all');
    this.filters.set(NO_FILTERS);
  }

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }

  retry(): void {
    this.snapshot.reload();
  }

  /* ----------------------------------------------------------------- inline edits ---- */

  /** Which row is open for editing. One at a time: two half-finished rows is two chances to lose one. */
  readonly editingId = signal<number | null>(null);

  startEdit(row: ProductRow): void {
    this.editingId.set(row.id);
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  saveEdit(edit: InlineProductEdit): void {
    const query = this.query();
    if (!query) {
      return;
    }
    this.busy.set(true);
    this.api.applyInlineEdit(edit, query).subscribe({
      next: (snapshot) => {
        this.busy.set(false);
        this.editingId.set(null);
        this.loaded.set(snapshot);
        this.toast.success(this.transloco.translate('products.saved.row'));
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  /**
   * The availability toggle in the row.
   *
   * Its own path rather than opening the inline editor, because it is one boolean and asking an
   * operator to open an editor, flip a switch and save to unpublish a product is three steps for
   * one decision. It still sends all four fields — see `ProductsApi.applyInlineEdit`.
   */
  toggleAvailable(row: ProductRow): void {
    this.saveEdit({id: row.id, price: row.price, quantity: row.quantity, available: !row.available});
  }

  /* -------------------------------------------------------------------- deletion ---- */

  readonly pendingDelete = signal<PendingDelete | null>(null);

  askDelete(row: ProductRow): void {
    this.pendingDelete.set({id: row.id, name: row.name});
  }

  dismissDelete(): void {
    this.pendingDelete.set(null);
  }

  confirmDelete(): void {
    const pending = this.pendingDelete();
    const query = this.query();
    this.pendingDelete.set(null);
    if (!pending || !query) {
      return;
    }
    this.busy.set(true);
    this.api.delete(pending.id, query).subscribe({
      next: (snapshot) => {
        this.busy.set(false);
        this.loaded.set(snapshot);
        this.toast.success(this.transloco.translate('products.saved.deleted', {name: pending.name}));
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  /** Called by the form after a save, so returning to the list does not show a stale row. */
  invalidate(): void {
    this.cache.invalidate();
  }
}
