import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {Router, provideRouter} from '@angular/router';
import {Observable, of} from 'rxjs';

import {NOTIFICATION_PORT} from '@cvhome-saas/ui-kit';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import {emptyDraft, type ProductDraft, type ProductImageItem, type RelatedProduct, type VariantMatrixRow} from '@models/products';
import type {PersistableVariantSet} from '@models/catalog';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';
import {provideFakeProductSearch} from '@testing/product-search.fake';
import {translocoTesting} from '@testing/transloco-testing';
import {ToastService} from '@cvhome-saas/ui-kit/ui';
import {ProductForm} from './product-form';
import {
  ProductFormApi,
  type ProductFormSnapshot,
  type VariantSaveOutcome,
} from './services/product-form.api.service';

const LANGUAGES = ['en', 'ar'];

function draft(over: Partial<ProductDraft> = {}): ProductDraft {
  return {...emptyDraft(LANGUAGES), ...over};
}

/** A saved, fully filled product — every readiness item ticked. */
const SAVED: ProductDraft = draft({
  id: 7,
  sku: 'ACM-7',
  visible: true,
  price: 750,
  quantity: 25,
  brandCode: 'NIKE',
  typeCode: 'SHOES',
  categoryIds: [1],
  copy: [
    {language: 'en', name: 'Runner', description: 'Fast', friendlyUrl: 'runner', title: '', metaDescription: '', highlights: '', keyWords: ''},
    {language: 'ar', name: 'عداء', description: '', friendlyUrl: '', title: '', metaDescription: '', highlights: '', keyWords: ''},
  ],
  images: [
    {id: 4, mediaAssetId: 4, name: 'a.jpg', url: null, altText: null, order: 0, isDefault: true},
    {id: 5, mediaAssetId: 5, name: 'b.jpg', url: null, altText: null, order: 1, isDefault: false},
  ],
});

function snapshot(product: ProductDraft): ProductFormSnapshot {
  return {
    draft: product,
    categories: [
      {id: 1, label: 'MEN', depth: 0},
      {id: 11, label: 'MEN_SHOES', depth: 1},
    ],
    brands: [{code: 'NIKE', label: 'Nike'}],
    types: [{code: 'SHOES', label: 'Shoes'}],
    languages: LANGUAGES,
    currency: 'SAR',
    vocabulary: API_REF?.vocabulary ?? [],
    assignedOptionIds: API_REF?.assignedOptionIds ?? [],
    variants: API_REF?.variants ?? [],
    variantsUnavailable: API_REF?.variantsUnavailable ?? false,
    vocabularyUnavailable: API_REF?.vocabularyUnavailable ?? false,
  };
}

/** The fake under test, so the snapshot builder above can answer its configured variant state. */
let API_REF: FakeProductFormApi | null = null;

class FakeProductFormApi {
  loaded: ProductDraft = SAVED;
  /** The vocabulary and saved variant state `load` answers. Overridden by the variants specs. */
  vocabulary: ProductFormSnapshot['vocabulary'] = [
    {
      id: 9,
      code: 'color',
      name: 'Color',
      values: [
        {id: 91, code: 'red', name: 'Red'},
        {id: 92, code: 'blue', name: 'Blue'},
      ],
    },
    {
      id: 10,
      code: 'size',
      name: 'Size',
      values: [
        {id: 101, code: 'm', name: 'M'},
        {id: 102, code: 'l', name: 'L'},
      ],
    },
  ];
  assignedOptionIds: readonly number[] = [];
  variants: ProductFormSnapshot['variants'] = [];
  variantsUnavailable = false;
  vocabularyUnavailable = false;
  /** Every variant-set replace the facade sent, so a spec can read the atomic body. */
  readonly variantSets: {set: PersistableVariantSet; rows: readonly VariantMatrixRow[]}[] = [];
  variantInventoryApplied = true;

  /** What the failed inventory leg reported it was writing — the retry must replay exactly this. */
  variantRemovedSkus: readonly string[] = [];

  saveVariants(
    _id: number,
    set: PersistableVariantSet,
    rows: readonly VariantMatrixRow[],
  ): Observable<VariantSaveOutcome> {
    this.variantSets.push({set, rows});
    return of({
      variantsApplied: true,
      inventoryApplied: this.variantInventoryApplied,
      pendingInventory: this.variantInventoryApplied
        ? null
        : {rows, removedSkus: this.variantRemovedSkus},
    });
  }

  /**
   * Declared with its real parameters on purpose: the fake used to take none, which is why the
   * retry silently dropping `removedSkus` could not be caught by any test written against it.
   */
  readonly inventoryWrites: {rows: readonly VariantMatrixRow[]; removedSkus: readonly string[]}[] = [];

  inventoryRetrySucceeds = true;

  applyVariantInventory(
    _id: number,
    rows: readonly VariantMatrixRow[],
    removedSkus: readonly string[],
  ): Observable<boolean> {
    this.inventoryWrites.push({rows, removedSkus});
    return of(this.inventoryRetrySucceeds);
  }
  /** How many times the snapshot was actually fetched. See the request-storm spec below. */
  loads = 0;
  readonly created: ProductDraft[] = [];
  readonly updated: {id: number; draft: ProductDraft}[] = [];
  categoriesApplied = true;

  load(id: number | null): Observable<ProductFormSnapshot> {
    this.loads += 1;
    return of(snapshot(id === null ? draft() : this.loaded));
  }

  create(product: ProductDraft): Observable<{id: number; categoriesApplied: boolean}> {
    this.created.push(product);
    return of({id: 7, categoriesApplied: this.categoriesApplied});
  }

  update(id: number, product: ProductDraft): Observable<{snapshot: ProductFormSnapshot; categoriesApplied: boolean}> {
    this.updated.push({id, draft: product});
    return of({snapshot: snapshot(this.loaded), categoriesApplied: this.categoriesApplied});
  }

  skuTaken = (_sku: string): Observable<boolean> => of(this.skuExists);
  /** The server's honest answer about a saved product's own SKU: yes, something has it. */
  skuExists = false;

  searchProducts(): Observable<readonly RelatedProduct[]> {
    return of([]);
  }

  uploadImages(): Observable<readonly ProductImageItem[]> {
    return of([]);
  }

  removeImage(): Observable<readonly ProductImageItem[]> {
    return of([]);
  }

  /** What a reorder actually sent, so a spec can read the flags off it. */
  readonly replaced: (readonly ProductImageItem[])[] = [];

  replaceImages(_id: number, ordered: readonly ProductImageItem[]): Observable<readonly ProductImageItem[]> {
    this.replaced.push(ordered);
    return of(ordered);
  }

  attachImages(): Observable<readonly ProductImageItem[]> {
    return of([]);
  }

  addRelated(): Observable<readonly RelatedProduct[]> {
    return of([]);
  }

  removeRelated(): Observable<readonly RelatedProduct[]> {
    return of([]);
  }
}

describe('ProductForm', () => {
  let api: FakeProductFormApi;
  let fixture: ComponentFixture<ProductForm>;
  let toasts: {messages: string[]; danger(text: string): void};

  beforeEach(async () => {
    localStorage.removeItem('cvhome.console.store');
    api = new FakeProductFormApi();
    API_REF = api;
    toasts = {messages: [], danger(text: string) { this.messages.push(text); }};

    await TestBed.configureTestingModule({
      imports: [ProductForm, ...translocoTesting().imports],
      providers: [
        provideRouter([]),
        /*
         * The Media step's library picker reaches `MediaService`, which is an `HttpClient` client.
         * Without these the step's whole panel fails to render — the injector throws inside change
         * detection and Angular swallows it, so the symptom is an empty step rather than an error.
         */
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: ConsoleApi, useValue: Object.assign(new FakeConsoleApi(), {stores: CONSOLE_STORES_FAKE})},
        {provide: ProductFormApi, useValue: api},
        /*
         * The form tells the list its rows are stale after a save. Stubbed here rather than let the
         * real one drag `ProductsApi` and `HttpClient` into a spec about a form.
         */
        {provide: NOTIFICATION_PORT, useValue: toasts},
        provideFakeProductSearch(),
        ...translocoTesting().providers,
      ],
    }).compileComponents();
  });

  /**
   * `id` undefined is `/products/new`; a string is `/products/:id`.
   *
   * Two settle cycles, because the route param reaches the facade through an effect: the first pass
   * sets `productId`, which is what the resource keys on, and only then does the load run.
   */
  function load(id?: string): HTMLElement {
    fixture = TestBed.createComponent(ProductForm);
    if (id !== undefined) {
      fixture.componentRef.setInput('id', id);
    }
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  /**
   * Fills the SKU and a name in every language.
   *
   * A save is refused while any supported language has no name — the populator replaces a product's
   * description list wholesale, so a blank language is a cleared language — which means a spec about
   * saving has to get past that first or it is testing the refusal instead.
   */
  function nameEveryLanguage(sku: string): void {
    const form = fixture.componentInstance['facade'].form;
    form.controls.sku.setValue(sku);
    for (const row of form.controls.copy.controls) {
      row.controls.name.setValue('Runner');
    }
    fixture.detectChanges();
  }

  function steps(element: HTMLElement): HTMLButtonElement[] {
    return [...element.querySelectorAll<HTMLButtonElement>('app-stepper .step')];
  }

  function headerButton(element: HTMLElement, label: string): HTMLButtonElement {
    return [...element.querySelectorAll<HTMLButtonElement>('app-page-header button')].find(
      (button) => button.textContent?.includes(label),
    )!;
  }

  it('loads the product once, not once per signal that settles', fakeAsync(() => {
    /*
     * The resource is keyed on the route id and the current store, and both arrive asynchronously
     * and independently. Keyed directly, it ran three times for one page — with no id and no store,
     * again when the route effect landed, again when the store directory resolved — and each run is
     * a `forkJoin` of six requests, two rounds of which the browser then cancelled mid-flight.
     * Eighteen requests to answer six questions.
     */
    load('7');

    expect(api.loads).toBe(1);
  }));

  it('renders the wizard and none of the console chrome', fakeAsync(() => {
    const element = load('7');

    expect(element.querySelector('app-stepper')).not.toBeNull();
    expect(element.querySelector('.toolbar')).toBeNull();
    expect(steps(element).length).toBe(5);
  }));

  it('locks Media until the product exists, and says why on the rail', fakeAsync(() => {
    const element = load();

    // Images post to `…/product/{id}/image`; there is nothing to attach them to yet.
    expect(steps(element)[1].disabled).toBe(true);
    expect(steps(element)[1].textContent).toContain('Save the product first');
  }));

  it('unlocks Media once the product has been saved', fakeAsync(() => {
    const element = load('7');

    expect(steps(element)[1].disabled).toBe(false);
  }));

  it('holds the related-products block back until there is a product to relate', fakeAsync(() => {
    const element = load();
    steps(element)[3].click();
    fixture.detectChanges();

    expect(element.textContent).toContain('Save the product first');
    expect(element.querySelector('app-autocomplete')).toBeNull();
  }));

  it('creates on Save draft and moves to the product it just made', fakeAsync(() => {
    const router = TestBed.inject(Router);
    const navigate = spyOn(router, 'navigate');
    const element = load();

    nameEveryLanguage('ACM-9');
    headerButton(element, 'Save draft').click();
    tick();

    expect(api.created.length).toBe(1);
    // Not published: Save draft is how a new product comes into existence, not how it goes live.
    expect(api.created[0].visible).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/products', 7]);
  }));

  it('routes to the new product even when its categories could not be applied', fakeAsync(() => {
    /*
     * The product exists. Reporting a failed create is how an operator retries into a duplicate-SKU
     * error for something they were told was never created.
     */
    const router = TestBed.inject(Router);
    const navigate = spyOn(router, 'navigate');
    api.categoriesApplied = false;
    const element = load();

    nameEveryLanguage('ACM-9');
    headerButton(element, 'Save draft').click();
    tick();

    expect(navigate).toHaveBeenCalledWith(['/products', 7]);
  }));

  it('refuses to publish while a required item is unticked, and says so', fakeAsync(() => {
    const element = load();

    headerButton(element, 'Publish').click();
    tick();
    fixture.detectChanges();

    expect(api.created.length).toBe(0);
    expect(element.querySelector('.publish-note')).not.toBeNull();
  }));

  it('publishes a product that has everything', fakeAsync(() => {
    const element = load('7');

    headerButton(element, 'Publish').click();
    tick();

    expect(api.updated.length).toBe(1);
    expect(api.updated[0].draft.visible).toBe(true);
  }));

  it('computes the checklist from the form, not from a response', fakeAsync(() => {
    const element = load();
    const done = () =>
      [...element.querySelectorAll('.check')].filter((item) => item.classList.contains('done')).length;

    const before = done();
    fixture.componentInstance['facade'].form.controls.sku.setValue('ACM-9');
    fixture.detectChanges();

    // No request went out; the panel moved because the field did.
    expect(done()).toBe(before + 1);
  }));

  it('names the locale a product is missing rather than showing a blank row', fakeAsync(() => {
    api.loaded = draft({
      ...SAVED,
      copy: SAVED.copy.map((row) => (row.language === 'ar' ? {...row, name: ''} : row)),
    });
    const element = load('7');

    const locales = [...element.querySelectorAll('.locale')].map((row) => row.textContent!.replace(/\s+/g, ' ').trim());
    expect(locales.some((row) => row.includes('Arabic') && row.includes('Not written'))).toBe(true);
    expect(locales.some((row) => row.includes('English') && row.includes('of'))).toBe(true);
  }));

  it('refuses to save while a language has no name, and says which', fakeAsync(() => {
    /*
     * The populator replaces a product's description list wholesale, so a language saved blank is a
     * language cleared — the storefront then has nothing at all to render for those shoppers. The
     * refusal names the language because the empty field is not on the step the operator is standing
     * on; "fill in the required fields" would send them hunting through a form that looks complete.
     */
    api.loaded = draft({
      ...SAVED,
      copy: SAVED.copy.map((row) => (row.language === 'ar' ? {...row, name: ''} : row)),
    });
    const element = load('7');

    headerButton(element, 'Save draft').click();
    tick();

    expect(api.updated.length).toBe(0);
    const toast = TestBed.inject(ToastService);
    expect(toast.messages().some((message) => message.text.includes('Arabic'))).toBe(true);
  }));

  it('sends a non-numeric id back to the list rather than to the API', fakeAsync(() => {
    const router = TestBed.inject(Router);
    const navigate = spyOn(router, 'navigate');

    load('nonsense');

    expect(navigate).toHaveBeenCalledWith(['/products'], {replaceUrl: true});
  }));

  it('never warns that a saved product duplicates its own SKU', fakeAsync(() => {
    /*
     * The check runs while the form is filled and the facade disables the SKU a tick later, so the
     * answer arrives on a disabled control. `disable()` nulls the errors present at the time and
     * cannot null one in flight — which is how every existing product carried a duplicate warning
     * about itself, on a field it does not allow editing.
     */
    api.skuExists = true;
    const element = load('7');
    tick(1000);
    fixture.detectChanges();

    const sku = fixture.componentInstance['facade'].form.controls.sku;
    expect(sku.disabled).toBe(true);
    expect(sku.hasError('skuTaken')).toBe(false);
    expect(element.querySelector('.check-taken')).toBeNull();
  }));

  it('edits the description with the rich editor, because the stored value is HTML', fakeAsync(() => {
    const element = load('7');

    expect(element.querySelector('app-rich-text')).not.toBeNull();
    // The one field on the platform that holds a document. Everything else is plain.
    expect(element.querySelector('#product-highlights')?.tagName).toBe('INPUT');
  }));

  it('uses the console’s own date picker rather than a native date field', fakeAsync(() => {
    const element = load('7');

    expect(element.querySelector('app-date-picker')).not.toBeNull();
    expect(element.querySelector('input[type="date"]')).toBeNull();
  }));

  it('edits keywords as chips over the one comma-separated column', fakeAsync(() => {
    const element = load('7');

    expect(element.querySelector('app-tag-input')).not.toBeNull();
  }));

  /*
   * The storefront thumbnail is the first image, and reordering is the only thing that sets it.
   *
   * It used to be pinned to whichever image was uploaded first, because the upload endpoint fixed it
   * and nothing could move it. `PUT …/product/{id}/images` writes the order and the flag together,
   * so that is no longer true — and leaving it pinned meant a reorder carried the badge off
   * position 1, under a panel that says in as many words that the first image is the thumbnail.
   *
   * Driven through the facade rather than the step's buttons: the rule is the facade's, and the
   * buttons are already covered by their own labels.
   */
  it('makes the first image the storefront thumbnail when the order changes', fakeAsync(() => {
    load('7');
    const facade = fixture.componentInstance['facade'];

    facade.moveImage(1, -1);
    tick();

    const sent = api.replaced.at(-1)!;
    expect(sent.map((image) => image.id)).toEqual([5, 4]);
    expect(sent.map((image) => image.isDefault)).toEqual([true, false]);
  }));

  /* ------------------------------------------------------------------- variants ---- */

  /** The facade under test — the variants step is state-driven, so the specs drive the state. */
  function facade() {
    return fixture.componentInstance['facade'];
  }

  it('generates the cartesian matrix when an axis is picked, seeding the first row', fakeAsync(() => {
    load('7');

    facade().addVariantAxis(9); // Color: red, blue
    const rows = facade().variantRows();

    expect(rows.length).toBe(2);
    // The first combination inherits the product's own sku, price and stock — a simple product's
    // numbers carry over instead of silently starting from zero.
    expect(rows[0].sku).toBe('ACM-7');
    expect(rows[0].price).toBe(750);
    expect(rows[0].isDefault).toBe(true);
    // The rest are suggested `<sku>-<VALUECODE>` and left unpriced for an explicit decision.
    expect(rows[1].sku).toBe('ACM-7-BLUE');
    expect(rows[1].price).toBeNull();

    facade().addVariantAxis(10); // × Size: m, l
    expect(facade().variantRows().length).toBe(4);
  }));

  it('sends the axes and the combinations as one atomic set, exactly one default', fakeAsync(() => {
    load('7');
    facade().addVariantAxis(9);
    facade().updateVariantRow(1, {price: 30});

    facade().saveVariants();
    tick();

    const [{set}] = api.variantSets;
    expect(set.options).toEqual(['color']);
    expect(set.variants.length).toBe(2);
    expect(set.variants.map((variant) => variant.sortOrder)).toEqual([0, 1]);
    expect(set.variants.filter((variant) => variant.defaultVariant).length).toBe(1);
    expect(set.variants[1].optionValueIds).toEqual([92]);
  }));

  /*
   * The four regressions below are the data-loss paths review found. Each was reachable in one
   * click on a normal workflow, and each shipped because nothing here drove it.
   */

  it('keeps an unsaved matrix when the header saves the draft', fakeAsync(() => {
    // The matrix and the definition are edited on one screen and saved by different buttons.
    // While both signals were linked to the snapshot, `Save draft` reset the matrix to the
    // server's and reported success — the operator lost the whole thing to a green toast.
    load('7');
    facade().addVariantAxis(9);
    facade().updateVariantRow(0, {price: 11});
    facade().updateVariantRow(1, {price: 22});

    facade().saveDraft();
    tick();

    expect(facade().variantAxes().length).toBe(1);
    expect(facade().variantRows().map((row) => row.price)).toEqual([11, 22]);
  }));

  it('carries price and stock onto the wider combinations when an axis is added', fakeAsync(() => {
    /*
     * Rows used to be kept only on an exact signature match, which can never hold when the axis
     * SET changes — so adding Size to a priced colour-only product nulled every price, and the
     * save then deleted the old inventory rows and wrote nothing back (unpriced rows are skipped).
     */
    load('7');
    facade().addVariantAxis(9); // Color: red, blue
    facade().updateVariantRow(0, {price: 10, quantity: 3});
    facade().updateVariantRow(1, {price: 12, quantity: 4});

    facade().addVariantAxis(10); // × Size: m, l

    const rows = facade().variantRows();
    expect(rows.length).toBe(4);
    expect(rows.map((row) => row.price)).toEqual([10, 10, 12, 12]);
    expect(rows.map((row) => row.quantity)).toEqual([3, 3, 4, 4]);
    // Each is a genuinely new combination, so none of them claims an existing catalog row...
    expect(rows.every((row) => row.id === null)).toBe(true);
    // ...and each gets its own sku rather than four rows fighting over two.
    expect(new Set(rows.map((row) => row.sku)).size).toBe(4);
  }));

  it('retries the inventory write it actually attempted, not whatever the screen holds', fakeAsync(() => {
    /*
     * The retry used to re-send `variantRows()` with no removed skus — and because the facade
     * reloaded the snapshot on the failure branch, those rows had already reverted to the
     * server's. So it wrote the old prices back and orphaned every retired sku's row for good.
     */
    load('7');
    facade().addVariantAxis(9);
    facade().updateVariantRow(0, {price: 99});
    facade().updateVariantRow(1, {price: 77});
    api.variantInventoryApplied = false;
    api.variantRemovedSkus = ['ACM-7-OLD'];

    facade().saveVariants();
    tick();

    expect(facade().variantInventoryPending()).toBe(true);
    // The operator's numbers are still on screen; nothing reverted under the banner.
    expect(facade().variantRows().map((row) => row.price)).toEqual([99, 77]);

    api.inventoryRetrySucceeds = true;
    facade().retryVariantInventory();
    tick();

    const [write] = api.inventoryWrites;
    expect(write.rows.map((row) => row.price)).toEqual([99, 77]);
    expect(write.removedSkus).toEqual(['ACM-7-OLD']);
    expect(facade().variantInventoryPending()).toBe(false);
  }));

  it('refuses to replace a variant set it could not read', fakeAsync(() => {
    /*
     * `null` (the read failed) used to collapse into `[]` (this product sells as one sku), so the
     * step invited a whole-set replace over combinations it had never seen — and retired none of
     * their inventory rows either, because the loaded sku list was empty too.
     */
    api.variantsUnavailable = true;
    const danger = spyOn(TestBed.inject(ToastService), 'danger');
    load('7');

    expect(facade().canSaveVariants()).toBe(false);

    facade().addVariantAxis(9);
    facade().saveVariants();
    tick();

    expect(api.variantSets.length).toBe(0);
    // and the step says so rather than looking like it worked
    expect(danger).toHaveBeenCalled();
  }));

  it('refuses a duplicate sku by name instead of letting the pod 409', fakeAsync(() => {
    load('7');
    facade().addVariantAxis(9);
    facade().updateVariantRow(1, {sku: 'ACM-7'});

    facade().saveVariants();
    tick();

    expect(api.variantSets.length).toBe(0);
  }));

  it('reassigns the default when its row is removed', fakeAsync(() => {
    load('7');
    facade().addVariantAxis(9);

    facade().removeVariantRow(0);

    const rows = facade().variantRows();
    expect(rows.length).toBe(1);
    expect(rows[0].isDefault).toBe(true);
  }));

  it('swaps the price readiness item for the per-variant one while options are assigned', fakeAsync(() => {
    load('7');

    expect(facade().readiness().some((item) => item.key === 'price')).toBe(true);

    facade().addVariantAxis(9);
    const items = facade().readiness();
    expect(items.some((item) => item.key === 'price')).toBe(false);
    const variantPricing = items.find((item) => item.key === 'variantPricing');
    // One row is seeded with the product's price, the other is not — publishing stays blocked.
    expect(variantPricing?.done).toBe(false);
    expect(facade().canPublish()).toBe(false);

    facade().updateVariantRow(1, {price: 30});
    expect(facade().readiness().find((item) => item.key === 'variantPricing')?.done).toBe(true);
  }));

  it('renders the matrix on the variants step, one row per combination', fakeAsync(() => {
    const element = load('7');
    facade().addVariantAxis(9);
    facade().activeStep.set('variants');
    fixture.detectChanges();

    const rows = [...element.querySelectorAll('.matrix tbody tr')];
    expect(rows.length).toBe(2);
    expect(rows[0].textContent).toContain('Red');
    expect(rows[1].textContent).toContain('Blue');
    // The step carries its own save: a different transaction than the header's Save draft.
    expect(element.querySelector('.matrix-foot .primary-action')?.textContent).toContain('Save variants');
  }));
});
