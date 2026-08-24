import {Component, ElementRef, computed, inject, signal, viewChild} from '@angular/core';
import {Router} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {ImageBroken} from '@shared/directives/image-broken';
import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {ConfirmDialog} from '@shared/ui/confirm-dialog/confirm-dialog';
import {DataTable, type TableColumn} from '@shared/ui/data-table/data-table';
import {ExportButton} from '@shared/ui/export-button/export-button';
import {TableRow} from '@shared/ui/data-table/table-row';
import {Icon} from '@shared/ui/icon/icon';
import {EmptyState} from '@shared/ui/empty-state/empty-state';
import {LoadError} from '@shared/ui/load-error/load-error';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Pagination} from '@shared/ui/pagination/pagination';
import {Panel} from '@shared/ui/panel/panel';
import {SearchBox} from '@shared/ui/search-box/search-box';
import {Select, type SelectOption} from '@shared/ui/select/select';
import {TabSwitcher} from '@shared/ui/tab-switcher/tab-switcher';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {Money} from '@shared/i18n/money';
import {NumberField} from '@shared/ui/number-field/number-field';
import type {InlineProductEdit, ProductRow, ProductTab} from '@models/products';
import {PAGE_SIZE, ProductsFacade} from './facades/products.facade';

/**
 * The table's columns.
 *
 * Seven, against the design's ten. Gone: On hand / Reserved / Available as three separate figures —
 * the platform stores one quantity — the stock bar and its "reorder at N" caption, the Value
 * column, and the row checkbox that fed a bulk bar. None has anything behind it. See lessons.md.
 */
const COLUMN_KEYS: readonly {key: string; labelKey: string; width: string; align?: 'start' | 'end'}[] = [
  {key: 'product', labelKey: 'products.column.product', width: 'minmax(12rem, 2.2fr)'},
  {key: 'category', labelKey: 'products.column.category', width: 'minmax(8rem, 1.2fr)'},
  {key: 'brand', labelKey: 'products.column.brand', width: 'minmax(7rem, 1fr)'},
  {key: 'price', labelKey: 'products.column.price', width: '7rem', align: 'end'},
  {key: 'quantity', labelKey: 'products.column.quantity', width: '6rem', align: 'end'},
  {key: 'available', labelKey: 'products.column.available', width: '7rem'},
  {key: 'actions', labelKey: '', width: '7rem'},
];

/**
 * The product list.
 *
 * Built from `Inventory.dc.html`, which is a warehouse screen this platform cannot serve: no
 * locations, no reorder points, no purchase orders, no stock movements, no valuation. What survives
 * is the stock-levels table itself, which is a product list — and that is what this is.
 *
 * **The KPI row is gone rather than reported unavailable.** All four tiles were unbacked, and a row
 * of four em dashes is a decoration, not an honest page. The one real figure the row carried — the
 * total SKU count — is in the header's context line and in the pagination footer.
 *
 * **Inline editing has a visible affordance.** seller-ui edited price and quantity on
 * double-click, discoverable only through a `title` tooltip. Here the row has an Edit button, and
 * pressing it turns three cells into inputs.
 */
@Component({
  providers: [ProductsFacade],
  selector: 'app-products',
  imports: [
    SearchBox,
    EmptyState,
    LoadError,
    Badge,
    BusyOverlay,
    ConfirmDialog,
    DataTable,
    ExportButton,
    Icon,
    ImageBroken,
    PageHeader,
    Pagination,
    NumberField,
    Panel,
    Select,
    TabSwitcher,
    TableRow,
    TranslocoDirective,
  ],
  templateUrl: './products.html',
  styleUrl: './products.css',
})
export class Products {
  private readonly router = inject(Router);
  private readonly transloco = inject(TranslocoService);
  private readonly money = inject(Money);
  private readonly localeFormat = inject(TranslocoLocaleService);

  /*
   * TODO(lessons.md): the low-stock banner, the "reorder at N" caption and the stock bar are not
   * built — nothing on the platform holds a replenishment level. See lessons.md, "Catalogue — no
   * reorder point and no low-stock threshold". The quantity column is the whole stock story this
   * platform can tell.
   */
  protected readonly facade = inject(ProductsFacade);

  protected readonly pageSize = PAGE_SIZE;

  constructor() {
    // The facade is root-provided and its resource waits for this, so that opening the product form
    // — which needs the facade only to invalidate it — does not fetch a list nobody is looking at.
    this.facade.active.set(true);
  }

  /** The region the export captures. Absent until the first response renders it. */
  /*
   * `{read: ElementRef}` is load-bearing.
   *
   * A template reference on a *component* element resolves to the component instance, not to an
   * element — so `#report` on `<app-panel>` handed `app-export-button` a `Panel` object, which is
   * neither an `ElementRef` nor an `HTMLElement`, and Export on the products page did nothing at
   * all. Orders, order details and the dashboard happened to anchor theirs on a plain `<div>` and
   * so happened to work. Asking for the element explicitly is correct on either anchor.
   */
  protected readonly report = viewChild('report', {read: ElementRef});
  protected readonly heading = this.facade.heading;

  protected readonly columns = computed<readonly TableColumn[]>(() => {
    this.transloco.activeLang();
    return COLUMN_KEYS.map((column) => ({
      key: column.key,
      label: column.labelKey ? this.transloco.translate(column.labelKey) : '',
      width: column.width,
      align: column.align,
    }));
  });

  /**
   * What the open editor holds, before it is sent.
   *
   * Local to the page rather than in the facade: it is unsaved keystrokes, not application state,
   * and it must not survive a navigation away from the list.
   */
  /*
   * Numbers, not strings. `app-number-field` parses as it goes and answers `null` for an empty box,
   * which is the distinction this editor has always needed and used to make by hand from a string.
   */
  protected readonly draftPrice = signal<number | null>(null);
  protected readonly draftQuantity = signal<number | null>(null);

  /**
   * Rows whose thumbnail would not load.
   *
   * Keyed by product id rather than by URL, so a filter change that reorders the table does not
   * resurrect a thumbnail already known to be unreachable.
   */
  protected readonly brokenThumbs = signal<ReadonlySet<number>>(new Set());

  protected markBroken(id: number): void {
    this.brokenThumbs.update((current) => new Set(current).add(id));
  }

  protected priceLabel(row: ProductRow): string {
    return this.money.format(row.price, this.facade.currency());
  }

  /**
   * The quantity, in the reader's own numerals.
   *
   * It used to render raw, so an Arabic operator saw `٧٥٠٫٠٠ ر.س.` in the price column and `25` in
   * the one beside it. The *editor* stays Latin on purpose — nobody types `٢٥`.
   */
  protected quantityLabel(row: ProductRow): string {
    this.transloco.activeLang();
    return this.localeFormat.localizeNumber(row.quantity, 'decimal');
  }

  protected startEdit(row: ProductRow): void {
    this.draftPrice.set(row.price);
    this.draftQuantity.set(row.quantity);
    this.facade.startEdit(row);
  }

  /**
   * Commit the open editor.
   *
   * An empty or unparseable price is sent as the row's existing one rather than as zero: a cleared
   * field is a mistake, and "price it at nothing" is not something anyone means by deleting three
   * digits.
   */
  protected commitEdit(row: ProductRow): void {
    const price = this.draftPrice();
    const quantity = this.draftQuantity();
    const edit: InlineProductEdit = {
      id: row.id,
      price: price ?? row.price,
      quantity: quantity !== null && quantity >= 0 ? Math.trunc(quantity) : row.quantity,
      available: row.available,
    };
    this.facade.saveEdit(edit);
  }

  protected onTabChange(key: string): void {
    this.facade.activeTab.set(key as ProductTab);
  }

  /*
   * The filter choices as data, because `app-select` takes options rather than projected `<option>`
   * elements. The leading entry is "all", whose empty value the handlers below map back to `null`.
   */
  protected readonly categoryOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return [
      {value: '', label: this.transloco.translate('products.filter.allCategories')},
      ...this.facade.categories().map((category) => ({
        value: String(category.id),
        label: category.label,
      })),
    ];
  });

  protected readonly brandOptions = computed<readonly SelectOption[]>(() => {
    this.transloco.activeLang();
    return [
      {value: '', label: this.transloco.translate('products.filter.allBrands')},
      ...this.facade.brands().map((brand) => ({value: String(brand.id), label: brand.label})),
    ];
  });

  protected onCategoryFilter(value: string): void {
    this.facade.setFilter('categoryId', value === '' ? null : Number(value));
  }

  protected onBrandFilter(value: string): void {
    this.facade.setFilter('brandId', value === '' ? null : Number(value));
  }

  protected openProduct(row: ProductRow): void {
    this.router.navigate(['/products', row.id]);
  }

  protected createProduct(): void {
    this.router.navigate(['/products', 'new']);
  }

  protected deleteTitle(): string {
    const pending = this.facade.pendingDelete();
    return pending ? this.transloco.translate('products.delete.title', {name: pending.name}) : '';
  }
}
