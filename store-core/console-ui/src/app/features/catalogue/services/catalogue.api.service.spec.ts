import {TestBed} from '@angular/core/testing';
import {Observable, of, throwError} from 'rxjs';

import {CategoryService} from '@api/catalog/category.service';
import {ManufacturerService} from '@api/catalog/manufacturer.service';
import {ProductGroupService} from '@api/catalog/product-group.service';
import {ProductOptionService} from '@api/catalog/product-option.service';
import {ProductService} from '@api/catalog/product.service';
import {ProductTypeService} from '@api/catalog/product-type.service';
import {MerchantStoreService} from '@api/merchant/store.service';
import type {PageT} from '@cvhome-saas/ui-kit';
import type {
  PersistableCategory,
  ReadableCategory,
  ReadableManufacturer,
  ReadableProductGroup,
  ReadableProductOption,
  ReadableProductType,
} from '@models/catalog';
import {CatalogueApi} from './catalogue.api.service';

function page<T>(content: T[]): PageT<T> {
  return {size: 500, totalElements: content.length, totalPages: 1, pageNumber: 0, content};
}

function category(
  id: number,
  code: string,
  over: Partial<ReadableCategory> = {},
): ReadableCategory {
  return {
    id,
    code,
    visible: true,
    sortOrder: id,
    productCount: 0,
    descriptions: [{language: 'en', name: code}],
    description: {language: 'en', name: code},
    children: [],
    ...over,
  };
}

/**
 * A hierarchy with a branch worth rolling up: `electronics` holds no products itself, and its two
 * children hold four and six.
 */
const HIERARCHY: ReadableCategory[] = [
  category(1, 'electronics', {
    children: [
      category(11, 'audio', {productCount: 4, parent: {id: 1, code: 'electronics'}}),
      category(12, 'displays', {productCount: 6, parent: {id: 1, code: 'electronics'}, visible: false}),
    ],
  }),
];

class FakeCategoryService {
  failing = false;
  readonly updates: {id: number; body: PersistableCategory}[] = [];
  readonly visibility: {id: number; body: PersistableCategory}[] = [];
  readonly moves: {child: number; parent: number}[] = [];
  hierarchyCalls = 0;

  hierarchy(): Observable<PageT<ReadableCategory>> {
    this.hierarchyCalls += 1;
    if (this.failing) {
      return throwError(() => new Error('category-hierarchy is down'));
    }
    return of(page(HIERARCHY));
  }

  update(id: number, body: PersistableCategory): Observable<PersistableCategory> {
    this.updates.push({id, body});
    return of(body);
  }

  setVisible(id: number, body: PersistableCategory): Observable<void> {
    this.visibility.push({id, body});
    return of(undefined);
  }

  move(child: number, parent: number): Observable<void> {
    this.moves.push({child, parent});
    return of(undefined);
  }

  moveToRoot(child: number): Observable<void> {
    return this.move(child, -1);
  }
}

class FakeManufacturerService {
  failing = false;

  list(): Observable<PageT<ReadableManufacturer>> {
    if (this.failing) {
      return throwError(() => new Error('manufacturers is down'));
    }
    return of(
      page<ReadableManufacturer>([
        {
          id: 1,
          code: 'northwind-audio',
          order: 0,
          descriptions: [{language: 'en', name: 'Northwind Audio'}],
          description: {language: 'en', name: 'Northwind Audio'},
        },
      ]),
    );
  }
}

class FakeProductTypeService {
  failing = false;

  list(): Observable<PageT<ReadableProductType>> {
    if (this.failing) {
      return throwError(() => new Error('product types is down'));
    }
    return of(
      page<ReadableProductType>([
        {id: 1, code: 'simple', visible: true, allowAddToCart: true, descriptions: []},
      ]),
    );
  }
}

class FakeProductOptionService {
  failing = false;

  list(): Observable<PageT<ReadableProductOption>> {
    if (this.failing) {
      return throwError(() => new Error('product options is down'));
    }
    return of(
      page<ReadableProductOption>([
        {
          id: 9,
          code: 'color',
          name: 'Color',
          sortOrder: 0,
          descriptions: [{language: 'en', name: 'Color'}],
          // Deliberately out of order: the mapper must sort them, the server does not.
          values: [
            {id: 92, code: 'blue', name: 'Blue', sortOrder: 1, descriptions: [{language: 'en', name: 'Blue'}]},
            {id: 91, code: 'red', name: 'Red', sortOrder: 0, descriptions: [{language: 'en', name: 'Red'}]},
          ],
        },
      ]),
    );
  }
}

class FakeProductGroupService {
  failing = false;
  /** Only the by-code read fails: the list still answers, as a partial outage would. */
  getFails = false;
  readonly saved: unknown[] = [];
  group: ReadableProductGroup = {
    id: 1,
    code: 'featured',
    active: true,
    descriptions: [
      {language: 'en', name: 'Featured'},
      {language: 'ar', name: 'مميز'},
    ],
    products: [{id: 7, sku: 'ACM-7', descriptions: [], children: []} as never],
  };

  /**
   * The list, hollow — exactly as the pod answers it.
   *
   * `GET /private/products/groups` really does return `products: []` for every group no matter what
   * they contain; only `get(code)` populates it. The fake has to lie the same way the server does,
   * or the spec cannot catch the console reading members from the wrong response.
   */
  list(): Observable<PageT<ReadableProductGroup>> {
    if (this.failing) {
      return throwError(() => new Error('product groups is down'));
    }
    return of(page([{...this.group, products: []}]));
  }

  get(code: string): Observable<ReadableProductGroup> {
    if (this.failing || this.getFails) {
      return throwError(() => new Error('product groups is down'));
    }
    return of(code === this.group.code ? this.group : {...this.group, code, products: []});
  }

  save(body: unknown): Observable<unknown> {
    this.saved.push(body);
    return of(body);
  }
}

class FakeMerchantStoreService {
  supportedLanguages(): Observable<string[]> {
    return of(['en', 'ar']);
  }
}

describe('CatalogueApi', () => {
  let api: CatalogueApi;
  let categories: FakeCategoryService;
  let brands: FakeManufacturerService;
  let types: FakeProductTypeService;
  let groups: FakeProductGroupService;
  let options: FakeProductOptionService;

  beforeEach(() => {
    categories = new FakeCategoryService();
    brands = new FakeManufacturerService();
    types = new FakeProductTypeService();
    groups = new FakeProductGroupService();
    options = new FakeProductOptionService();

    TestBed.configureTestingModule({
      providers: [
        CatalogueApi,
        {provide: CategoryService, useValue: categories},
        {provide: ManufacturerService, useValue: brands},
        {provide: ProductTypeService, useValue: types},
        {provide: ProductGroupService, useValue: groups},
        {provide: ProductOptionService, useValue: options},
        {provide: ProductService, useValue: {}},
        {provide: MerchantStoreService, useValue: new FakeMerchantStoreService()},
      ],
    });
    api = TestBed.inject(CatalogueApi);
  });

  it('rolls a branch total up from its children', (done) => {
    api.load().subscribe((snapshot) => {
      const [root] = snapshot.categories;
      // Its own count is zero; what is under it is ten.
      expect(root.productCount).toBe(0);
      expect(root.totalCount).toBe(10);
      expect(root.children.map((child) => child.totalCount)).toEqual([4, 6]);
      done();
    });
  });

  it('keeps the tree when the four optional lists fail, and names which tabs are down', (done) => {
    brands.failing = true;
    types.failing = true;
    groups.failing = true;
    options.failing = true;

    api.load().subscribe((snapshot) => {
      expect(snapshot.categories.length).toBe(1);
      expect(snapshot.brands).toEqual([]);
      expect([...snapshot.unavailable].sort()).toEqual(['brands', 'groups', 'options', 'types']);
      done();
    });
  });

  it('orders an option’s values by their sort order, whatever order the server sent', (done) => {
    api.load().subscribe((snapshot) => {
      expect(snapshot.options[0].values.map((value) => value.code)).toEqual(['red', 'blue']);
      done();
    });
  });

  it('fails the whole load when the hierarchy fails — the tree is the page', (done) => {
    categories.failing = true;
    api.load().subscribe({
      next: () => fail('expected the load to fail'),
      error: (failure: Error) => {
        expect(failure.message).toContain('category-hierarchy');
        done();
      },
    });
  });

  it('sends the code and the descriptions the visibility patch is validated against', (done) => {
    api.load().subscribe(() => {
      api.setCategoryVisible(12, true).subscribe(() => {
        const [call] = categories.visibility;
        expect(call.id).toBe(12);
        expect(call.body.visible).toBe(true);
        // `PATCH …/visible` binds a `@Valid PersistableCategory`, so these two must be present or
        // the request is rejected before the handler sees it.
        expect(call.body.code).toBe('displays');
        expect(call.body.descriptions.length).toBe(1);
        done();
      });
    });
  });

  it('carries the parent through an edit, so a save never promotes a child to the root', (done) => {
    api.load().subscribe(() => {
      api
        .updateCategory(11, [{language: 'en', name: 'Audio', description: '', friendlyUrl: '', title: '', metaDescription: '', highlights: '', keyWords: ''}], {
          visible: true,
          sortOrder: 3,
        })
        .subscribe(() => {
          expect(categories.updates[0].body.parent).toEqual({id: 1, code: 'electronics'});
          done();
        });
    });
  });

  it('does not offer a sibling reorder — the write is broken and the read is unsorted', () => {
    /*
     * `sortOrder` is the only way to express sibling order. `PUT /private/category/{id}` 500s for
     * every caller (an immutable children list reaches Hibernate's merge), and the hierarchy does
     * not come back ordered by `sortOrder` regardless. See lessons.md.
     */
    expect('reorderCategory' in api).toBe(false);
  });

  it('promotes a category to the top level with the parent id the pod special-cases', (done) => {
    api.load().subscribe(() => {
      api.moveCategory(11, null).subscribe(() => {
        expect(categories.moves).toEqual([{child: 11, parent: -1}]);
        done();
      });
    });
  });

  it('reads a group’s members from the group, not from the list of groups', (done) => {
    /*
     * `GET /private/products/groups` answers `products: []` for every group whatever they hold; only
     * `GET /private/products/groups/{code}` populates it. Building the member lists from the list
     * response therefore showed every group as empty — and looked correct, because an empty list is
     * a perfectly plausible answer. seller-ui's group screen works for exactly this reason: it
     * fetches by code, one group at a time.
     */
    api.load().subscribe((snapshot) => {
      expect(snapshot.groups[0].members.map((member) => member.sku)).toEqual(['ACM-7']);
      done();
    });
  });

  it('keeps a group whose own record could not be read, rather than losing the tab', (done) => {
    // The name is right in the list response; only the members are unknown. That is worth showing.
    groups.getFails = true;

    api.load().subscribe((snapshot) => {
      expect(snapshot.groups.length).toBe(1);
      expect(snapshot.groups[0].members).toEqual([]);
      expect(snapshot.unavailable).not.toContain('groups');
      done();
    });
  });

  it('re-sends a group’s existing names when only its active flag changes', (done) => {
    api.load().subscribe(() => {
      api.setGroupActive('featured', false).subscribe(() => {
        const body = groups.saved[0] as {code: string; active: boolean; descriptions: unknown[]};
        expect(body.code).toBe('featured');
        expect(body.active).toBe(false);
        // The POST is an upsert that replaces the record: omitting these would clear both names.
        expect(body.descriptions.length).toBe(2);
        done();
      });
    });
  });

  it('never sends a group’s member list with a copy edit', (done) => {
    api.load().subscribe(() => {
      api
        .saveGroup('featured', [{language: 'en', name: 'Featured', description: '', friendlyUrl: '', title: '', metaDescription: '', highlights: '', keyWords: ''}], true)
        .subscribe(() => {
          expect('productIds' in (groups.saved[0] as object)).toBe(false);
          done();
        });
    });
  });

  it('reloads after every write rather than echoing the operator’s own input back', (done) => {
    api.load().subscribe(() => {
      expect(categories.hierarchyCalls).toBe(1);
      api.moveCategory(11, 12).subscribe(() => {
        expect(categories.hierarchyCalls).toBe(2);
        done();
      });
    });
  });
});
