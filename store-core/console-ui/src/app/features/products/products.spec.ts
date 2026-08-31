import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Observable, of, throwError} from 'rxjs';

import {NOTIFICATION_PORT} from '@core/errors/notification.port';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import type {InlineProductEdit, ProductRow, ProductsSnapshot} from '@models/products';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';
import {TranslocoService} from '@jsverse/transloco';

import {translocoTesting} from '@testing/transloco-testing';
import {Products} from './products';
import {PAGE_SIZE} from './facades/products.facade';
import {ProductsApi, type ProductsQuery} from './services/products.api.service';

function row(id: number, over: Partial<ProductRow> = {}): ProductRow {
  return {
    id,
    name: `Product ${id}`,
    sku: `ACM-${id}`,
    variantCount: 1,
    categories: ['Audio'],
    brand: 'Northwind',
    price: 129,
    quantity: 12,
    available: true,
    shipeable: true,
    imageUrl: null,
    ...over,
  };
}

const CATALOGUE: readonly ProductRow[] = [
  row(1),
  row(2, {name: 'Copy paper', sku: 'ACM-PPR', available: false, quantity: 0}),
  row(3, {name: 'Desk chair', sku: 'ACM-FUR', categories: []}),
];

/** Stands in for the endpoint so the spec controls filtering, paging and failure. */
class FakeProductsApi {
  readonly requests: ProductsQuery[] = [];
  readonly edits: InlineProductEdit[] = [];
  readonly deleted: number[] = [];
  failure = false;
  catalogue: readonly ProductRow[] = CATALOGUE;

  loadSnapshot(query: ProductsQuery): Observable<ProductsSnapshot> {
    this.requests.push(query);
    if (this.failure) {
      return throwError(() => new Error('Unable to load products.'));
    }
    return of(this.snapshot(query));
  }

  applyInlineEdit(edit: InlineProductEdit, query: ProductsQuery): Observable<ProductsSnapshot> {
    this.edits.push(edit);
    this.catalogue = this.catalogue.map((product) =>
      product.id === edit.id
        ? {...product, price: edit.price, quantity: edit.quantity, available: edit.available}
        : product,
    );
    return this.loadSnapshot(query);
  }

  delete(id: number, query: ProductsQuery): Observable<ProductsSnapshot> {
    this.deleted.push(id);
    this.catalogue = this.catalogue.filter((product) => product.id !== id);
    return this.loadSnapshot(query);
  }

  /** Filters and pages the catalogue the way the server would. */
  private snapshot(query: ProductsQuery): ProductsSnapshot {
    const {sku} = query.filters;
    // A substring match, the way `ProductRepository` builds its `LIKE %sku%`.
    const matching = this.catalogue.filter(
      (product) =>
        (query.tab === 'all' || product.available === (query.tab === 'available')) &&
        (!sku.trim() || product.sku.toLowerCase().includes(sku.trim().toLowerCase())),
    );
    const size = query.page.count;
    const totalPages = Math.max(1, Math.ceil(matching.length / size));
    const pageNumber = Math.min(Math.max(0, query.page.page), totalPages - 1);

    return {
      page: {
        size,
        totalElements: matching.length,
        totalPages,
        pageNumber,
        content: matching.slice(pageNumber * size, pageNumber * size + size),
      },
      categories: [{id: 11, label: 'Audio'}],
      brands: [{id: 1, label: 'Northwind'}],
      currency: 'SAR',
    };
  }
}

describe('Products', () => {
  let api: FakeProductsApi;
  let fixture: ComponentFixture<Products>;
  let toasts: {messages: string[]; danger(text: string): void};

  beforeEach(async () => {
    localStorage.removeItem('cvhome.console.store');
    api = new FakeProductsApi();
    toasts = {messages: [], danger(text: string) { this.messages.push(text); }};
    await TestBed.configureTestingModule({
      imports: [Products, ...translocoTesting().imports],
      providers: [
        provideRouter([]),
        {provide: ConsoleApi, useValue: Object.assign(new FakeConsoleApi(), {stores: CONSOLE_STORES_FAKE})},
        {provide: ProductsApi, useValue: api},
        {provide: NOTIFICATION_PORT, useValue: toasts},
        ...translocoTesting().providers,
      ],
    }).compileComponents();
  });

  function load(): HTMLElement {
    fixture = TestBed.createComponent(Products);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  function names(element: HTMLElement): string[] {
    return [...element.querySelectorAll('.product-name')].map((node) => node.textContent!.trim());
  }

  it('renders a page of products and none of the console chrome', fakeAsync(() => {
    const element = load();

    expect(names(element)).toEqual(['Product 1', 'Copy paper', 'Desk chair']);
    expect(element.querySelector('.toolbar')).toBeNull();
    expect(element.querySelector('.sidebar')).toBeNull();
  }));

  it('fetches nothing until the page that shows it is mounted', fakeAsync(() => {
    /*
     * The facade is page-provided, so nothing can construct it — and start its `rxResource` —
     * before the page mounts. The product form once injected the root-provided version of this
     * facade for one method, `invalidate`, and paid for a page of products it never displayed on
     * every visit to `/products/:id`.
     */
    expect(api.requests.length).toBe(0);

    load();
    expect(api.requests.length).toBe(1);
  }));

  it('asks for the first page of everything on load', fakeAsync(() => {
    load();

    expect(api.requests.length).toBe(1);
    expect(api.requests[0].tab).toBe('all');
    expect(api.requests[0].page).toEqual({page: 0, count: PAGE_SIZE});
  }));

  it('shows no KPI row — all four tiles the design draws are unbacked', fakeAsync(() => {
    const element = load();

    expect(element.querySelector('app-kpi-grid')).toBeNull();
    // The one real figure the row carried is in the header instead.
    expect(element.querySelector('app-page-header')?.textContent).toContain('3 products');
  }));

  it('names a product with no categories rather than leaving the cell blank', fakeAsync(() => {
    const element = load();

    const cells = [...element.querySelectorAll('.cell-category')].map((node) => node.textContent!.trim());
    expect(cells[2]).toBe('Uncategorised');
  }));

  it('narrows on the SKU filter and offers a way back', fakeAsync(() => {
    const element = load();

    const sku = element.querySelector('app-search-box input') as HTMLInputElement;
    sku.value = 'ACM-PPR';
    sku.dispatchEvent(new Event('input'));
    // The box debounces, so a filter does not reach the server per keystroke.
    fixture.detectChanges();
    tick(300);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    expect(names(element)).toEqual(['Copy paper']);
    expect(api.requests[api.requests.length - 1].filters.sku).toBe('ACM-PPR');

    // The way back is the cross inside the box itself, not a separate "clear filters" link:
    // every control in the filter row now clears the one thing it filters on.
    const clear = element.querySelector('app-search-box .search-clear') as HTMLButtonElement;
    clear.click();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    expect(names(element).length).toBe(3);
  }));

  it('distinguishes an empty filter from an empty catalogue', fakeAsync(() => {
    api.catalogue = [];
    const element = load();

    // Nothing in the store: the way out is to add a product, not to clear a filter.
    expect(element.querySelector('app-empty-state')?.textContent).toContain('no products yet');
    expect(element.querySelector('app-empty-state button')?.textContent?.trim()).toBe('Add product');

    api.catalogue = CATALOGUE;
    const sku = element.querySelector('app-search-box input') as HTMLInputElement;
    sku.value = 'NOTHING-AT-ALL';
    sku.dispatchEvent(new Event('input'));
    // The box debounces, so a filter does not reach the server per keystroke.
    fixture.detectChanges();
    tick(300);
    fixture.detectChanges();

    expect(element.querySelector('app-empty-state')?.textContent).toContain('No products match');
    expect(element.querySelector('app-empty-state button')?.textContent?.trim()).toBe('Clear filters');
  }));

  it('renders the quantity in the reader’s own numerals, like the price beside it', fakeAsync(() => {
    /*
     * It used to render raw, so an Arabic operator saw `٧٥٠٫٠٠ ر.س.` in the price column and `25`
     * in the one next to it. Only the header had been translated.
     */
    const element = load();
    const transloco = TestBed.inject(TranslocoService);
    const quantities = () =>
      [...element.querySelectorAll('.cell-quantity .numeric')].map((cell) => cell.textContent!.trim());

    expect(quantities()[0]).toBe('12');

    transloco.setActiveLang('ar');
    fixture.detectChanges();

    expect(quantities()[0]).toBe('١٢');
  }));

  it('keeps the filter controls compact rather than letting them take the table’s width', fakeAsync(() => {
    /*
     * `Inventory.dc.html` sizes the search box at a fixed 230px and its filters to their content.
     * Built as `flex: 1 1 11rem` these grew to 372px each — three controls wider than the first
     * three columns of the table they narrow.
     */
    const element = load();

    // They belong on the panel's header row, beside the tabs, not on a rail of their own below it.
    expect(element.querySelector('.panel-controls app-search-box')).not.toBeNull();
    expect(element.querySelector('.panel-controls app-tab-switcher')).not.toBeNull();
    expect(element.querySelector('.filter-rail')).toBeNull();

    /*
     * The width itself is not asserted here: the Karma window is narrower than the 900px breakpoint,
     * where the controls are *supposed* to stretch, so a computed `flex-grow` would test the
     * responsive rule rather than the desktop one. Measured in the browser instead — the search box
     * is 232px against the 372px it was.
     */
  }));

  it('turns three cells into inputs on an explicit Edit, not on a double-click', fakeAsync(() => {
    const element = load();

    expect(element.querySelector('.inline-input')).toBeNull();

    const edit = element.querySelector('.cell-actions .icon-action') as HTMLButtonElement;
    edit.click();
    fixture.detectChanges();

    // `.inline-input` is the `app-number-field` host now; the box you type in is inside it.
    expect(element.querySelectorAll('app-number-field.inline-input').length).toBe(2);
    const inputs = element.querySelectorAll('.inline-input input');
    expect(inputs.length).toBe(2);
    // No browser spinners, and no wheel-to-edit, anywhere a figure is typed.
    expect((inputs[0] as HTMLInputElement).type).toBe('text');

    (inputs[0] as HTMLInputElement).value = '99.5';
    inputs[0].dispatchEvent(new Event('input', {bubbles: true}));
    (inputs[1] as HTMLInputElement).value = '4';
    inputs[1].dispatchEvent(new Event('input', {bubbles: true}));
    fixture.detectChanges();

    const confirm = element.querySelector('.icon-action.confirm') as HTMLButtonElement;
    confirm.click();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    expect(api.edits).toEqual([{id: 1, price: 99.5, quantity: 4, available: true}]);
    expect(element.querySelector('.inline-input')).toBeNull();
  }));

  it('keeps the old price when the field is cleared rather than pricing the product at nothing', fakeAsync(() => {
    const element = load();

    (element.querySelector('.cell-actions .icon-action') as HTMLButtonElement).click();
    fixture.detectChanges();

    const price = element.querySelector('.inline-input input') as HTMLInputElement;
    price.value = '';
    price.dispatchEvent(new Event('input', {bubbles: true}));
    fixture.detectChanges();

    (element.querySelector('.icon-action.confirm') as HTMLButtonElement).click();
    fixture.detectChanges();
    tick();

    expect(api.edits[0].price).toBe(129);
  }));

  it('commits an availability change on the spot, sending the row’s other fields with it', fakeAsync(() => {
    const element = load();

    const toggle = element.querySelector('.availability') as HTMLButtonElement;
    toggle.click();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    expect(api.edits).toEqual([{id: 1, price: 129, quantity: 12, available: false}]);
  }));

  it('deletes only behind the confirm dialog', fakeAsync(() => {
    const element = load();

    const remove = element.querySelector('.icon-action.danger') as HTMLButtonElement;
    remove.click();
    fixture.detectChanges();

    expect(api.deleted).toEqual([]);
    const dialog = element.querySelector('app-confirm-dialog');
    expect(dialog?.textContent).toContain('Delete Product 1?');

    fixture.componentInstance['facade'].confirmDelete();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    expect(api.deleted).toEqual([1]);
    expect(names(element)).toEqual(['Copy paper', 'Desk chair']);
  }));

  it('offers a retry when the list fails', fakeAsync(() => {
    api.failure = true;
    const element = load();

    expect(element.querySelector('app-load-error')?.textContent).toContain('Unable to load products.');
    expect(element.querySelector('app-load-error button')).not.toBeNull();
  }));
});
