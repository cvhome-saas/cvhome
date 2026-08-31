import {TestBed} from '@angular/core/testing';
import {Observable, of, throwError} from 'rxjs';

import {CategoryService} from '@api/catalog/category.service';
import {ManufacturerService} from '@api/catalog/manufacturer.service';
import {ProductService, type ProductQuery} from '@api/catalog/product.service';
import {ProductTypeService} from '@api/catalog/product-type.service';
import {MerchantStoreService} from '@api/merchant/store.service';
import type {PageT} from '@core/table/table.types';
import type {
  LightPersistableProduct,
  ReadableCategory,
  ReadableManufacturer,
  ReadableProduct,
} from '@models/catalog';
import type {ReadableMerchantStore} from '@models/merchant';
import {NO_FILTERS} from '@models/products';
import {InventoryService} from '@api/inventory/inventory.service';
import type {SkuInventory} from '@models/catalog';
import {ProductsApi, type ProductsQuery} from './products.api.service';

/** One product shaped as `GET /api/v2/private/base-products` actually sends it. */
function product(id: number, over: Partial<ReadableProduct> = {}): ReadableProduct {
  return {
    id,
    sku: `ACM-${id}`,
    available: true,
    visible: true,
    productShipeable: true,
    description: {language: 'en', name: `Product ${id}`},
    manufacturer: {id: 1, code: 'northwind', descriptions: [{language: 'en', name: 'Northwind'}]},
    categories: [
      {id: 11, code: 'audio', descriptions: [], children: [], description: {language: 'en', name: 'Audio'}},
    ],
    ...over,
  };
}

function page<T>(content: T[]): PageT<T> {
  return {size: 20, totalElements: content.length, totalPages: 1, pageNumber: 0, content};
}

class FakeInventoryService {
  readonly upserts: {sku: string; body: unknown}[] = [];
  readonly deletes: number[] = [];
  inventories: SkuInventory[] = [
    {sku: 'ACM-1', productId: 1, available: true, canBePurchased: true, quantity: 12, price: {originalPrice: 129, finalPrice: 129, discounted: false, discountPercent: 0}},
  ];

  /** What the list actually calls: product-addressed, so a row can total its variants. */
  readonly askedFor: number[][] = [];

  byProducts(productIds: readonly number[]): Observable<readonly SkuInventory[]> {
    this.askedFor.push([...productIds]);
    return of(this.inventories);
  }

  bySkus(): Observable<readonly SkuInventory[]> {
    return of(this.inventories);
  }

  upsert(sku: string, body: unknown): Observable<unknown> {
    this.upserts.push({sku, body});
    return of({});
  }

  deleteByProduct(id: number): Observable<void> {
    this.deletes.push(id);
    return of(undefined);
  }
}

class FakeProductService {
  readonly queries: ProductQuery[] = [];
  readonly searches: unknown[] = [];
  readonly patches: {id: number; body: LightPersistableProduct}[] = [];
  products: ReadableProduct[] = [product(1), product(2, {available: false})];

  list(query: ProductQuery): Observable<PageT<ReadableProduct>> {
    this.queries.push(query);
    return of(page(this.products));
  }

  patch(id: number, body: LightPersistableProduct): Observable<void> {
    this.patches.push({id, body});
    return of(undefined);
  }

  search(query: unknown): Observable<PageT<ReadableProduct>> {
    this.searches.push(query);
    return of(page(this.products));
  }

  delete(): Observable<void> {
    return of(undefined);
  }
}

class FakeCategoryService {
  failing = false;

  hierarchy(): Observable<PageT<ReadableCategory>> {
    if (this.failing) {
      return throwError(() => new Error('category-hierarchy is down'));
    }
    const child: ReadableCategory = {
      id: 11,
      code: 'audio',
      descriptions: [{language: 'en', name: 'Audio'}],
      description: {language: 'en', name: 'Audio'},
      children: [],
    };
    return of(
      page<ReadableCategory>([
        {
          id: 1,
          code: 'electronics',
          descriptions: [{language: 'en', name: 'Electronics'}],
          description: {language: 'en', name: 'Electronics'},
          children: [child],
        },
      ]),
    );
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
        {id: 1, code: 'northwind', descriptions: [], description: {language: 'en', name: 'Northwind'}},
      ]),
    );
  }
}

class FakeMerchantStoreService {
  failing = false;

  store(): Observable<ReadableMerchantStore> {
    if (this.failing) {
      return throwError(() => new Error('store is down'));
    }
    return of({currency: 'SAR'} as ReadableMerchantStore);
  }
}

const QUERY: ProductsQuery = {tab: 'all', filters: NO_FILTERS, page: {page: 0, count: 20}};

describe('ProductsApi', () => {
  let api: ProductsApi;
  let inventory: FakeInventoryService;
  let products: FakeProductService;
  let categories: FakeCategoryService;
  let brands: FakeManufacturerService;
  let stores: FakeMerchantStoreService;

  beforeEach(() => {
    inventory = new FakeInventoryService();
    products = new FakeProductService();
    categories = new FakeCategoryService();
    brands = new FakeManufacturerService();
    stores = new FakeMerchantStoreService();

    TestBed.configureTestingModule({
      providers: [
        ProductsApi,
        {provide: ProductService, useValue: products},
        {provide: InventoryService, useValue: inventory},
        {provide: CategoryService, useValue: categories},
        {provide: ManufacturerService, useValue: brands},
        {provide: ProductTypeService, useValue: {}},
        {provide: MerchantStoreService, useValue: stores},
      ],
    });
    api = TestBed.inject(ProductsApi);
  });

  it('maps a product onto a row, naming its category and its brand from the same response', (done) => {
    api.loadSnapshot(QUERY).subscribe((snapshot) => {
      const [first] = snapshot.page.content;
      expect(first.name).toBe('Product 1');
      expect(first.sku).toBe('ACM-1');
      expect(first.categories).toEqual(['Audio']);
      expect(first.brand).toBe('Northwind');
      expect(first.price).toBe(129);
      expect(snapshot.currency).toBe('SAR');
      done();
    });
  });

  it('totals a product’s stock across its variants, and prices it from the default one', (done) => {
    /*
     * The bug this pins: the row read the DEFAULT variant's quantity and reported it as the
     * product's, so a product with 12 + 8 + 4 across three combinations showed "12 in stock".
     * Price stays the default variant's — that is the merchant's own choice of the card price,
     * deliberately not a range and not the cheapest combination.
     */
    inventory.inventories = [
      {sku: 'ACM-1', productId: 1, available: true, canBePurchased: true, quantity: 12,
        price: {originalPrice: 129, finalPrice: 129, discounted: false, discountPercent: 0}},
      {sku: 'ACM-1-L', productId: 1, available: true, canBePurchased: true, quantity: 8,
        price: {originalPrice: 139, finalPrice: 139, discounted: false, discountPercent: 0}},
      {sku: 'ACM-1-XL', productId: 1, available: true, canBePurchased: true, quantity: 4,
        price: {originalPrice: 149, finalPrice: 149, discounted: false, discountPercent: 0}},
    ];

    api.loadSnapshot(QUERY).subscribe((snapshot) => {
      const [first] = snapshot.page.content;
      expect(first.quantity).toBe(24);
      expect(first.price).toBe(129);
      // One product-addressed call for the whole page — never one per product, never per sku.
      expect(inventory.askedFor.length).toBe(1);
      expect(inventory.askedFor[0]).toContain(1);
      done();
    });
  });

  it('falls back to the SKU for a product with no copy in the language asked for', (done) => {
    products.products = [product(3, {description: undefined})];
    api.loadSnapshot(QUERY).subscribe((snapshot) => {
      expect(snapshot.page.content[0].name).toBe('ACM-3');
      done();
    });
  });

  it('sends only the filters the operator has set, never an empty one', (done) => {
    api
      .loadSnapshot({
        ...QUERY,
        tab: 'available',
        filters: {sku: ' ACM-1 ', categoryId: 11, brandId: null},
      })
      .subscribe(() => {
        const query = products.queries[0];
        expect(query.available).toBe(true);
        expect(query.sku).toBe('ACM-1');
        // Never sent: `ProductCriteria.productName` is bound and read by nothing. See lessons.md.
        expect('productName' in query).toBe(false);
        expect(query.categoryIds).toBe(11);
        expect('manufacturerId' in query).toBe(false);
        // `count`, not `size` — the platform renames Spring's page-size parameter.
        expect(query.count).toBe(20);
        done();
      });
  });

  it('names a product from the row, because the list it reads actually carries one', (done) => {
    /*
     * Guards the endpoint choice. `/private/base-products` answers `description: null` on every row
     * — verified on the running stack — so a row built from it has no name at all. The service reads
     * `/products`, which runs the identical query through a mapper that populates the description,
     * the categories and the brand. See `ProductService.list`.
     */
    api.loadSnapshot(QUERY).subscribe((snapshot) => {
      expect(snapshot.page.content[0].name).not.toBe(snapshot.page.content[0].sku);
      expect(snapshot.page.content[0].categories.length).toBeGreaterThan(0);
      expect(snapshot.page.content[0].brand).not.toBeNull();
      done();
    });
  });

  it('omits the availability filter entirely on the All tab', (done) => {
    api.loadSnapshot(QUERY).subscribe(() => {
      expect('available' in products.queries[0]).toBe(false);
      done();
    });
  });

  it('indents the category options so the tree survives a flat select', (done) => {
    api.loadSnapshot(QUERY).subscribe((snapshot) => {
      expect(snapshot.categories.map((option) => option.label)).toEqual([
        'Electronics',
        '  Audio',
      ]);
      done();
    });
  });

  it('keeps the table when the two filter lists and the store all fail', (done) => {
    categories.failing = true;
    brands.failing = true;
    stores.failing = true;

    api.loadSnapshot(QUERY).subscribe((snapshot) => {
      expect(snapshot.page.content.length).toBe(2);
      expect(snapshot.categories).toEqual([]);
      expect(snapshot.brands).toEqual([]);
      // No currency means a plain number, which is better than the wrong symbol.
      expect(snapshot.currency).toBeNull();
      done();
    });
  });

  it('splits an inline edit: visibility to catalog, price and quantity to inventory', (done) => {
    api.loadSnapshot(QUERY).subscribe(() => {
      api
        .applyInlineEdit({id: 1, price: 99.5, quantity: 4, available: false}, QUERY)
        .subscribe(() => {
          expect(products.patches.length).toBe(1);
          expect(products.patches[0].id).toBe(1);
          expect(products.patches[0].body).toEqual({
            available: false,
            // Not edited on this screen, carried from the last response so it is not zeroed.
            productShipeable: true,
          });
          expect(inventory.upserts.length).toBe(1);
          expect(inventory.upserts[0].sku).toBe('ACM-1');
          expect(inventory.upserts[0].body).toEqual({
            productId: 1,
            quantity: 4,
            available: false,
            price: {amount: 99.5},
          });
          done();
        });
    });
  });

  it('sends a zero price rather than omitting it when a product has none', (done) => {
    api.loadSnapshot(QUERY).subscribe(() => {
      api.applyInlineEdit({id: 2, price: null, quantity: 0, available: true}, QUERY).subscribe(() => {
        const body = inventory.upserts[0].body as {price: {amount: number}};
        expect(body.price.amount).toBe(0);
        done();
      });
    });
  });
});
