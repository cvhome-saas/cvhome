import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Observable, of, throwError} from 'rxjs';

import {NOTIFICATION_PORT} from '@core/errors/notification.port';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import type {
  BrandCard,
  CatalogueSnapshot,
  CatalogueTab,
  CategoryNode,
  GroupRow,
  LocalisedCopy,
  TypeCard,
} from '@models/taxonomy';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';
import {provideFakeProductSearch} from '@testing/product-search.fake';
import {translocoTesting} from '@testing/transloco-testing';
import {Catalogue} from './catalogue';
import {CatalogueApi} from './services/catalogue.api.service';

function copy(language: string, name: string): LocalisedCopy {
  return {
    language,
    name,
    description: '',
    friendlyUrl: '',
    title: '',
    metaDescription: '',
    highlights: '',
    keyWords: '',
  };
}

function node(
  id: number,
  code: string,
  name: string,
  children: CategoryNode[] = [],
  over: Partial<CategoryNode> = {},
): CategoryNode {
  const own = over.productCount ?? 0;
  return {
    id,
    code,
    name,
    visible: true,
    sortOrder: id,
    depth: 0,
    parentId: null,
    productCount: own,
    totalCount: children.reduce((sum, child) => sum + child.totalCount, own),
    copy: [copy('en', name)],
    children,
    ...over,
  };
}

const AUDIO = node(11, 'audio', 'Audio', [], {parentId: 1, depth: 1, productCount: 4});
const DISPLAYS = node(12, 'displays', 'Displays', [], {parentId: 1, depth: 1, productCount: 6});

const BRAND: BrandCard = {
  id: 1,
  code: 'northwind-audio',
  name: 'Northwind Audio',
  description: 'Conference audio.',
  copy: [copy('en', 'Northwind Audio')],
  initials: 'NA',
};

const TYPE: TypeCard = {
  id: 1,
  code: 'simple',
  name: 'Simple product',
  description: 'One SKU, no variants.',
  visible: true,
  allowAddToCart: true,
  copy: [copy('en', 'Simple product')],
};

const GROUP: GroupRow = {
  code: 'featured',
  name: 'Featured',
  active: true,
  copy: [copy('en', 'Featured')],
  members: [{id: 7, name: 'Wireless headphones', sku: 'ACM-7'}],
};

function snapshot(over: Partial<CatalogueSnapshot> = {}): CatalogueSnapshot {
  return {
    categories: [node(1, 'electronics', 'Electronics', [AUDIO, DISPLAYS])],
    brands: [BRAND],
    types: [TYPE],
    groups: [GROUP],
    languages: ['en', 'ar'],
    unavailable: [],
    ...over,
  };
}

/** Stands in for the endpoints so the spec controls the tree, the writes and failure. */
class FakeCatalogueApi {
  loads = 0;
  failure = false;
  current: CatalogueSnapshot = snapshot();
  readonly moves: {nodeId: number; parentId: number | null}[] = [];
  readonly visibility: {id: number; visible: boolean}[] = [];
  readonly deleted: (number | string)[] = [];

  load(): Observable<CatalogueSnapshot> {
    this.loads += 1;
    if (this.failure) {
      return throwError(() => new Error('Unable to load the catalogue.'));
    }
    return of(this.current);
  }

  moveCategory(nodeId: number, parentId: number | null): Observable<CatalogueSnapshot> {
    this.moves.push({nodeId, parentId});
    return this.load();
  }

  setCategoryVisible(id: number, visible: boolean): Observable<CatalogueSnapshot> {
    this.visibility.push({id, visible});
    return this.load();
  }

  deleteCategory(id: number): Observable<CatalogueSnapshot> {
    this.deleted.push(id);
    return this.load();
  }

  deleteBrand(id: number): Observable<CatalogueSnapshot> {
    this.deleted.push(id);
    return this.load();
  }

  deleteType(id: number): Observable<CatalogueSnapshot> {
    this.deleted.push(id);
    return this.load();
  }

  deleteGroup(code: string): Observable<CatalogueSnapshot> {
    this.deleted.push(code);
    return this.load();
  }
}

describe('Catalogue', () => {
  let api: FakeCatalogueApi;
  let fixture: ComponentFixture<Catalogue>;

  beforeEach(async () => {
    localStorage.removeItem('cvhome.console.store');
    api = new FakeCatalogueApi();
    await TestBed.configureTestingModule({
      imports: [Catalogue, ...translocoTesting().imports],
      providers: [
        provideRouter([]),
        {provide: ConsoleApi, useValue: Object.assign(new FakeConsoleApi(), {stores: CONSOLE_STORES_FAKE})},
        {provide: CatalogueApi, useValue: api},
        {provide: NOTIFICATION_PORT, useValue: {danger: () => undefined}},
        provideFakeProductSearch(),
        ...translocoTesting().providers,
      ],
    }).compileComponents();
  });

  function load(tab: CatalogueTab = 'categories'): HTMLElement {
    fixture = TestBed.createComponent(Catalogue);
    fixture.componentRef.setInput('tab', tab);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  function labels(element: HTMLElement): string[] {
    return [...element.querySelectorAll('.node-label')].map((node) => node.textContent!.trim());
  }

  it('renders the hierarchy as a tree, deepest last, and none of the console chrome', fakeAsync(() => {
    const element = load();

    expect(labels(element)).toEqual(['Electronics', 'Audio', 'Displays']);
    expect(element.querySelector('.toolbar')).toBeNull();
  }));

  it('shows the branch total rather than a parent’s own empty count', fakeAsync(() => {
    const element = load();

    const counts = [...element.querySelectorAll('.node-meta')].map((node) => node.textContent!.trim());
    expect(counts).toEqual(['10', '4', '6']);
  }));

  it('gives the tree one tab stop, not one per row', fakeAsync(() => {
    const element = load();

    const focusable = [...element.querySelectorAll('.tree-row')].filter(
      (row) => row.getAttribute('tabindex') === '0',
    );
    expect(focusable.length).toBe(1);
  }));

  it('carries the hierarchy into ARIA, so the shape survives without the indent', fakeAsync(() => {
    const element = load();

    const rows = [...element.querySelectorAll('.tree-row')];
    expect(rows.map((row) => row.getAttribute('aria-level'))).toEqual(['1', '2', '2']);
    expect(rows[0].getAttribute('aria-expanded')).toBe('true');
    // A leaf is not expandable, and must not claim to be.
    expect(rows[1].getAttribute('aria-expanded')).toBeNull();
  }));

  it('re-parents by row button as well as by drag — the keyboard path is not a lesser one', fakeAsync(() => {
    const element = load();

    // The third row is `Displays`; "move into the row above" nests it under `Audio`.
    const rows = [...element.querySelectorAll('.tree-row')];
    const nest = rows[2].querySelectorAll('.row-moves .icon-action')[0] as HTMLButtonElement;
    nest.click();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    expect(api.moves).toEqual([{nodeId: 12, parentId: 11}]);
  }));

  it('offers no sibling reordering, because the platform cannot express it', fakeAsync(() => {
    /*
     * Two independent blockers: `PUT /private/category/{id}` 500s for every caller, and the
     * hierarchy does not come back in `sortOrder` order anyway. Up/down controls would be a gesture
     * that changes nothing. See lessons.md.
     */
    const element = load();

    const actions = [...element.querySelectorAll('.tree-row')[2].querySelectorAll('.row-moves .icon-action')];
    const labels = actions.map((button) => button.getAttribute('aria-label'));

    expect(labels.some((label) => label?.includes('up among'))).toBe(false);
    expect(labels.some((label) => label?.includes('down among'))).toBe(false);
    // What is left is what works: nest, unnest, add, and the row's own menu.
    expect(labels.some((label) => label?.includes('into the row above'))).toBe(true);
  }));

  it('promotes a child to the top level through its own endpoint', fakeAsync(() => {
    const element = load();

    const rows = [...element.querySelectorAll('.tree-row')];
    const out = rows[1].querySelectorAll('.row-moves .icon-action')[1] as HTMLButtonElement;
    out.click();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    expect(api.moves).toEqual([{nodeId: 11, parentId: null}]);
  }));

  it('toggles storefront visibility from the row without saving the editor beside it', fakeAsync(() => {
    const element = load();

    const eye = element.querySelectorAll('.tree-row .icon-action')[0] as HTMLButtonElement;
    eye.click();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    expect(api.visibility).toEqual([{id: 1, visible: false}]);
  }));

  it('warns before deleting a branch, because the delete cascades', fakeAsync(() => {
    const element = load();

    expect(element.querySelector('app-notice-bar')?.textContent).toContain('Deleting it deletes them too');
  }));

  it('deletes only behind the confirm dialog', fakeAsync(() => {
    const element = load();

    const remove = [...element.querySelectorAll('.danger-action')][0] as HTMLButtonElement;
    remove.click();
    fixture.detectChanges();

    expect(api.deleted).toEqual([]);
    expect(element.querySelector('app-confirm-dialog')?.textContent).toContain('Delete Electronics?');

    fixture.componentInstance['facade'].confirmDelete();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    expect(api.deleted).toEqual([1]);
  }));

  it('opens the tab the route names, and counts each one', fakeAsync(() => {
    const element = load('brands');

    expect(element.querySelector('app-brand-tab')).not.toBeNull();
    expect(element.querySelector('app-category-tab')).toBeNull();
    const badges = [...element.querySelectorAll('.tab-badge')].map((node) => node.textContent!.trim());
    expect(badges).toEqual(['3', '1', '1', '1']);
  }));

  it('shows a brand’s initials — there is no logo on the record to show', fakeAsync(() => {
    const element = load('brands');

    expect(element.querySelector('.brand-mark')?.textContent?.trim()).toBe('NA');
    expect(element.querySelector('app-image-picker')).toBeNull();
  }));

  it('offers a brand no slug and no sort order, because neither is persisted', fakeAsync(() => {
    /*
     * `manufacturer_description` has no `sef_url` column and
     * `PersistableManufacturerPopulator` never reads `order`. Both controls used to be here and
     * both silently discarded what was typed into them. See lessons.md.
     */
    const element = load('brands');

    expect(element.querySelector('#brand-order')).toBeNull();
    expect(element.querySelector('#brand-slug')).toBeNull();
    expect(element.textContent).toContain('is a name and a description');
  }));

  it('says a product type carries no attributes rather than drawing an empty panel', fakeAsync(() => {
    const element = load('types');

    expect(element.textContent).toContain('does not define which attributes');
  }));

  it('holds group membership outside the form, and says so', fakeAsync(() => {
    const element = load('groups');

    // Members are removable chips now, capped in height so adding one does not move the page.
    expect(element.querySelector('.picker-chip-name')?.textContent?.trim()).toBe('Wireless headphones');
    expect(element.textContent).toContain('not part of Save');
  }));

  it('reports a tab whose list failed instead of showing it empty', fakeAsync(() => {
    api.current = snapshot({brands: [], unavailable: ['brands']});
    const element = load('brands');

    expect(element.querySelector('app-notice-bar')?.textContent).toContain('could not be loaded');
    // No count on a tab the console could not read — zero would be a claim it cannot make.
    const badges = [...element.querySelectorAll('.tab-badge')].map((node) => node.textContent!.trim());
    expect(badges).toEqual(['3', '1', '1']);
  }));

  it('offers a retry when the hierarchy fails, because the tree is the page', fakeAsync(() => {
    api.failure = true;
    const element = load();

    expect(element.querySelector('app-load-error')?.textContent).toContain('Unable to load the catalogue.');
    expect(element.querySelector('app-category-tab')).toBeNull();
  }));
});
