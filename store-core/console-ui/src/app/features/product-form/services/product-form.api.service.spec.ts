import {TestBed} from '@angular/core/testing';
import {Observable, of, throwError} from 'rxjs';

import {CategoryService} from '@api/catalog/category.service';
import {ManufacturerService} from '@api/catalog/manufacturer.service';
import {ProductImageService} from '@api/catalog/product-image.service';
import {ProductRelationshipService} from '@api/catalog/product-relationship.service';
import {ProductService} from '@api/catalog/product.service';
import {ProductTypeService} from '@api/catalog/product-type.service';
import {MerchantStoreService} from '@api/merchant/store.service';
import type {PageT} from '@core/table/table.types';
import type {
  PersistableProductDefinition,
  ReadableCategory,
  ReadableImage,
  ReadableManufacturer,
  ReadableProductDefinition,
  ReadableProductType,
} from '@models/catalog';
import type {ReadableMerchantStore} from '@models/merchant';
import {emptyDraft, type ProductDraft} from '@models/products';
import {InventoryService} from '@api/inventory/inventory.service';
import type {SkuInventory} from '@models/catalog';
import {ProductFormApi} from './product-form.api.service';

function page<T>(content: T[]): PageT<T> {
  return {size: 500, totalElements: content.length, totalPages: 1, pageNumber: 0, content};
}

function category(id: number, code: string): ReadableCategory {
  return {id, code, descriptions: [{language: 'en', name: code}], children: []};
}

/** A product as `GET /api/v2/private/product/{id}` sends it, with two categories already on it. */
const DEFINITION: ReadableProductDefinition = {
  id: 7,
  sku: 'ACM-7',
  visible: true,
  shipeable: true,
  virtual: false,
  dateAvailable: '2026-03-04T00:00:00Z',
  sortOrder: 2,
  productSpecifications: {weight: 0.4, weightUnitOfMeasure: 'KG', dimensionUnitOfMeasure: 'CM'},
  type: {id: 1, code: 'SHOES', descriptions: []},
  manufacturer: {id: 3, code: 'NIKE', descriptions: null},
  categories: [category(1, 'MEN'), category(11, 'MEN_SHOES')],
  images: [
    {id: 5, imageName: 'b.jpg', order: 1, defaultImage: false},
    {id: 4, imageName: 'a.jpg', order: 0, defaultImage: true},
  ],
  descriptions: [
    {language: 'en', name: 'Runner', description: '<p>Fast</p>', friendlyUrl: 'runner'},
  ],
};

class FakeInventoryService {
  readonly upserts: {sku: string; body: unknown}[] = [];
  inventories: SkuInventory[] = [
    {
      sku: 'ACM-7',
      available: true,
      canBePurchased: true,
      quantity: 25,
      price: {finalPrice: 750, originalPrice: 750},
    },
  ];

  bySkus(): Observable<readonly SkuInventory[]> {
    return of(this.inventories);
  }

  upsert(sku: string, body: unknown): Observable<unknown> {
    this.upserts.push({sku, body});
    return of({});
  }
}

class FakeProductService {
  readonly created: PersistableProductDefinition[] = [];
  readonly updated: {id: number; body: PersistableProductDefinition}[] = [];
  readonly categoryAdds: {productId: number; categoryId: number}[] = [];
  readonly categoryRemoves: {productId: number; categoryId: number}[] = [];
  /** Set to a category id to make that one write fail, the way a partial batch does. */
  failCategory: number | null = null;

  definition(): Observable<ReadableProductDefinition> {
    return of(DEFINITION);
  }

  create(body: PersistableProductDefinition): Observable<{id: number}> {
    this.created.push(body);
    return of({id: 7});
  }

  update(id: number, body: PersistableProductDefinition): Observable<void> {
    this.updated.push({id, body});
    return of(undefined);
  }

  addToCategory(productId: number, categoryId: number): Observable<void> {
    if (this.failCategory === categoryId) {
      return throwError(() => new Error('category write failed'));
    }
    this.categoryAdds.push({productId, categoryId});
    return of(undefined);
  }

  removeFromCategory(productId: number, categoryId: number): Observable<void> {
    this.categoryRemoves.push({productId, categoryId});
    return of(undefined);
  }

  search(): Observable<PageT<never>> {
    return of(page<never>([]));
  }

  skuTaken(): Observable<{exists: boolean}> {
    return of({exists: false});
  }
}

class FakeImageService {
  readonly uploads: {order: number; defaultImage: boolean}[] = [];
  readonly reorders: {imageId: number; order: number}[] = [];
  gallery: ReadableImage[] = [...DEFINITION.images];
  failUploadAt: number | null = null;

  upload(_productId: number, _file: File, order: number, defaultImage: boolean): Observable<void> {
    if (this.failUploadAt === order) {
      return throwError(() => new Error('upload failed'));
    }
    this.uploads.push({order, defaultImage});
    return of(undefined);
  }

  reorder(_productId: number, imageId: number, order: number): Observable<void> {
    this.reorders.push({imageId, order});
    return of(undefined);
  }

  remove(): Observable<void> {
    return of(undefined);
  }

  images(): Observable<ReadableImage[]> {
    return of(this.gallery);
  }
}

class FakeRelationshipService {
  related(): Observable<{descriptions: never[]; products: never[]}> {
    return of({descriptions: [], products: []});
  }
}

class FakeCategoryService {
  hierarchy(): Observable<PageT<ReadableCategory>> {
    return of(page([{...category(1, 'MEN'), children: [category(11, 'MEN_SHOES')]}]));
  }
}

class FakeManufacturerService {
  list(): Observable<PageT<ReadableManufacturer>> {
    // `descriptions: null` on purpose — that is what the wire sends for a manufacturer.
    return of(page<ReadableManufacturer>([{id: 3, code: 'NIKE', descriptions: null}]));
  }
}

class FakeProductTypeService {
  list(): Observable<PageT<ReadableProductType>> {
    return of(page<ReadableProductType>([{id: 1, code: 'SHOES', descriptions: []}]));
  }
}

class FakeStoreService {
  store(): Observable<ReadableMerchantStore> {
    return of({
      id: 'ORG1-STORE1',
      name: 'Acme Supply Co.',
      currency: 'SAR',
      supportedLanguages: ['en', 'ar'],
      defaultLanguage: 'en',
    } as ReadableMerchantStore);
  }
}

describe('ProductFormApi', () => {
  let api: ProductFormApi;
  let inventory: FakeInventoryService;
  let products: FakeProductService;
  let images: FakeImageService;

  beforeEach(() => {
    inventory = new FakeInventoryService();
    products = new FakeProductService();
    images = new FakeImageService();

    TestBed.configureTestingModule({
      providers: [
        ProductFormApi,
        {provide: ProductService, useValue: products},
        {provide: InventoryService, useValue: inventory},
        {provide: ProductImageService, useValue: images},
        {provide: ProductRelationshipService, useValue: new FakeRelationshipService()},
        {provide: CategoryService, useValue: new FakeCategoryService()},
        {provide: ManufacturerService, useValue: new FakeManufacturerService()},
        {provide: ProductTypeService, useValue: new FakeProductTypeService()},
        {provide: MerchantStoreService, useValue: new FakeStoreService()},
      ],
    });
    api = TestBed.inject(ProductFormApi);
  });

  /* ----------------------------------------------------------------------- reading ---- */

  it('reads a definition into the shape the form binds to', (done) => {
    api.load(7).subscribe(({draft}) => {
      expect(draft.sku).toBe('ACM-7');
      // `Instant` on the wire, `YYYY-MM-DD` in a date field.
      expect(draft.dateAvailable).toBe('2026-03-04');
      // Price and quantity come from the inventory service since the split.
      expect(draft.price).toBe(750);
      expect(draft.quantity).toBe(25);
      expect(draft.brandCode).toBe('NIKE');
      expect(draft.typeCode).toBe('SHOES');
      expect(draft.categoryIds).toEqual([1, 11]);
      done();
    });
  });

  it('sorts the gallery by the order the pod stored, not the order it answered in', (done) => {
    api.load(7).subscribe(({draft}) => {
      expect(draft.images.map((image) => image.name)).toEqual(['a.jpg', 'b.jpg']);
      expect(draft.images[0].isDefault).toBe(true);
      done();
    });
  });

  it('gives every store language a copy row, so a missing translation has somewhere to be typed', (done) => {
    api.load(7).subscribe(({draft}) => {
      expect(draft.copy.map((copy) => copy.language).sort()).toEqual(['ar', 'en']);
      expect(draft.copy.find((copy) => copy.language === 'ar')?.name).toBe('');
      done();
    });
  });

  it('answers a blank draft for a product that does not exist yet', (done) => {
    api.load(null).subscribe(({draft}) => {
      expect(draft.id).toBeNull();
      // A new product is not on the storefront until the operator says so — Save draft needs this.
      expect(draft.visible).toBe(false);
      done();
    });
  });

  /* ----------------------------------------------------------------------- writing ---- */

  function draftFrom(over: Partial<ProductDraft> = {}): ProductDraft {
    return {...emptyDraft(['en']), sku: 'ACM-9', ...over};
  }

  it('sends the brand and the type as codes, never as ids', (done) => {
    /*
     * The one place on this platform a relation is addressed by code. An id here resolves to no
     * manufacturer and silently drops the brand.
     */
    api.create(draftFrom({brandCode: 'NIKE', typeCode: 'SHOES'})).subscribe(() => {
      expect(products.created[0].manufacturer).toBe('NIKE');
      expect(products.created[0].type).toBe('SHOES');
      done();
    });
  });

  it('widens the date to an Instant, and omits it entirely when there is none', (done) => {
    api.create(draftFrom({dateAvailable: '2026-05-06'})).subscribe(() => {
      expect(products.created[0].dateAvailable).toBe('2026-05-06T00:00:00Z');

      api.create(draftFrom({dateAvailable: ''})).subscribe(() => {
        // Not `''`, which does not parse as an Instant.
        expect('dateAvailable' in products.created[1]).toBe(false);
        done();
      });
    });
  });

  it('drops a language with nothing written in it', (done) => {
    const draft = draftFrom({
      copy: [
        {language: 'en', name: 'Runner', description: '', friendlyUrl: '', title: '', metaDescription: '', highlights: '', keyWords: ''},
        {language: 'ar', name: '', description: '', friendlyUrl: '', title: '', metaDescription: '', highlights: '', keyWords: ''},
      ],
    });

    api.create(draft).subscribe(() => {
      // An empty description would overwrite whatever the storefront falls back to with a blank.
      expect(products.created[0].descriptions.map((entry) => entry.language)).toEqual(['en']);
      done();
    });
  });

  it('never sends the category list on the product itself', (done) => {
    api.create(draftFrom({categoryIds: [1, 11]})).subscribe(() => {
      // The join is maintained by the two dedicated endpoints, which `applyCategories` calls.
      expect(products.created[0].categories).toBeUndefined();
      done();
    });
  });

  /* --------------------------------------------------------------- category diffing ---- */

  it('writes only the difference, as adds and removes', (done) => {
    api.load(7).subscribe(() => {
      // Was [1, 11]; now [1, 12].
      api.update(7, draftFrom({categoryIds: [1, 12]})).subscribe(() => {
        expect(products.categoryAdds).toEqual([{productId: 7, categoryId: 12}]);
        expect(products.categoryRemoves).toEqual([{productId: 7, categoryId: 11}]);
        done();
      });
    });
  });

  it('writes nothing at all when the set has not changed', (done) => {
    api.load(7).subscribe(() => {
      api.update(7, draftFrom({categoryIds: [1, 11]})).subscribe(() => {
        expect(products.categoryAdds).toEqual([]);
        expect(products.categoryRemoves).toEqual([]);
        done();
      });
    });
  });

  it('adds every chosen category on a create, because a new product is in none', (done) => {
    api.create(draftFrom({categoryIds: [1, 11]})).subscribe(() => {
      expect(products.categoryAdds.map((call) => call.categoryId)).toEqual([1, 11]);
      done();
    });
  });

  /* ------------------------------------------------------------- partial failures ---- */

  it('still answers the new id when the category diff fails after the product was created', (done) => {
    /*
     * The product exists. Reporting a failed create is how an operator ends up retrying into a
     * duplicate-SKU error for a product they were told did not exist.
     */
    products.failCategory = 11;

    api.create(draftFrom({categoryIds: [11]})).subscribe({
      next: ({id, categoriesApplied}) => {
        expect(id).toBe(7);
        expect(categoriesApplied).toBe(false);
        done();
      },
      error: () => fail('the create must not report failure once the product exists'),
    });
  });

  it('still reloads when the category diff fails after a save', (done) => {
    api.load(7).subscribe(() => {
      products.failCategory = 12;

      api.update(7, draftFrom({categoryIds: [1, 11, 12]})).subscribe(({snapshot, categoriesApplied}) => {
        expect(categoriesApplied).toBe(false);
        // The form now shows what the server has, not what the operator typed.
        expect(snapshot.draft.categoryIds).toEqual([1, 11]);
        done();
      });
    });
  });

  /* ------------------------------------------------------------------------ images ---- */

  it('claims the default only for the first image on a product that has none', (done) => {
    images.gallery = [];

    api.uploadImages(7, [new File([''], 'a.jpg'), new File([''], 'b.jpg')], []).subscribe(() => {
      expect(images.uploads).toEqual([
        {order: 0, defaultImage: true},
        {order: 1, defaultImage: false},
      ]);
      done();
    });
  });

  it('never claims the default when the product already has one', (done) => {
    /*
     * `buildContentImages` sets the flag without clearing it on the old default, so asking for it
     * twice leaves two. See lessons.md.
     */
    api
      .uploadImages(7, [new File([''], 'c.jpg')], [
        {id: 4, name: 'a.jpg', url: null, order: 0, isDefault: true},
      ])
      .subscribe(() => {
        expect(images.uploads).toEqual([{order: 1, defaultImage: false}]);
        done();
      });
  });

  it('renumbers the whole gallery on a reorder', (done) => {
    const ordered = [
      {id: 5, name: 'b.jpg', url: null, order: 1, isDefault: false},
      {id: 4, name: 'a.jpg', url: null, order: 0, isDefault: true},
    ];

    api.reorderImages(7, ordered).subscribe(() => {
      // `PATCH …?order=` does not renumber the images it displaces, so writing only the moved one
      // leaves two sharing a position.
      expect(images.reorders).toEqual([
        {imageId: 5, order: 0},
        {imageId: 4, order: 1},
      ]);
      done();
    });
  });

  it('reports a batch that failed partway rather than answering a half-truth', (done) => {
    /*
     * The first upload landed and the second did not. The caller must hear about it — recovering
     * the gallery is `ProductFormFacade.refreshGallery`, because only the facade has somewhere to
     * put the answer.
     */
    images.gallery = [];
    images.failUploadAt = 1;

    api.uploadImages(7, [new File([''], 'a.jpg'), new File([''], 'b.jpg')], []).subscribe({
      next: () => fail('a failed batch must not answer as though it succeeded'),
      error: (failure: Error) => {
        expect(failure.message).toContain('upload failed');
        expect(images.uploads).toEqual([{order: 0, defaultImage: true}]);
        done();
      },
    });
  });

  it('always ends with a read, so the pod decides the order and the default flag', (done) => {
    let subscribes = 0;
    const original = images.images.bind(images);
    images.images = () => {
      subscribes += 1;
      return original();
    };

    api.uploadImages(7, [new File([''], 'a.jpg')], []).subscribe((gallery) => {
      expect(subscribes).toBe(1);
      expect(gallery.map((image) => image.name)).toEqual(['a.jpg', 'b.jpg']);
      done();
    });
  });
});
