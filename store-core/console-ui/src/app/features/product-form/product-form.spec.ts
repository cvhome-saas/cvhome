import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {Router, provideRouter} from '@angular/router';
import {Observable, of} from 'rxjs';

import {NOTIFICATION_PORT} from '@core/errors/notification.port';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import {emptyDraft, type ProductDraft, type ProductImageItem, type RelatedProduct} from '@models/products';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';
import {provideFakeProductSearch} from '@testing/product-search.fake';
import {translocoTesting} from '@testing/transloco-testing';
import {ToastService} from '@shared/ui/toast/toast';
import {ProductForm} from './product-form';
import {ProductFormApi, type ProductFormSnapshot} from './services/product-form.api.service';

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
  images: [{id: 4, name: 'a.jpg', url: null, order: 0, isDefault: true}],
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
  };
}

class FakeProductFormApi {
  loaded: ProductDraft = SAVED;
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

  reorderImages(): Observable<readonly ProductImageItem[]> {
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
    toasts = {messages: [], danger(text: string) { this.messages.push(text); }};

    await TestBed.configureTestingModule({
      imports: [ProductForm, ...translocoTesting().imports],
      providers: [
        provideRouter([]),
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
    expect(steps(element).length).toBe(4);
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
});
