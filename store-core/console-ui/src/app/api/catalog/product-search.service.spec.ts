import {TestBed} from '@angular/core/testing';
import {Observable, of} from 'rxjs';

import {ProductService} from '@api/catalog/product.service';
import type {PageT} from '@core/table/table.types';
import {SelectedStoreService} from '@api/tenancy/selected-store.service';
import {ProductSearch} from '@api/catalog/product-search.service';

interface WireProduct {
  readonly id: number;
  readonly sku: string;
  readonly description: {readonly name: string} | null;
}

const CATALOGUE: readonly WireProduct[] = [
  {id: 1, sku: 'SKU-NK-RUN-001', description: {name: 'Nike ZoomX Invincible Run 3'}},
  {id: 2, sku: 'SKU-ZR-CL-DRS02', description: {name: 'Zara Satin Effect Midi Dress'}},
  {id: 3, sku: 'SKU-CF-MUG-003', description: {name: 'Café Mug'}},
  {id: 4, sku: 'SKU-NK-CL-KHD07', description: {name: 'Nike Club Fleece Hoodie'}},
  {id: 5, sku: 'SKU-XX-NON-005', description: null},
];

class FakeProductService {
  calls = 0;

  search(): Observable<PageT<never>> {
    this.calls += 1;
    return of({
      content: CATALOGUE,
      pageNumber: 0,
      size: CATALOGUE.length,
      totalElements: CATALOGUE.length,
      totalPages: 1,
      recordsFiltered: CATALOGUE.length,
    } as unknown as PageT<never>);
  }
}

/**
 * The open store, which is all this service needs from the rest of the app.
 *
 * It used to read `ConsoleShellFacade`, so this spec had to stand up the console shell's api
 * service and a router to search a list of five products. Reading `SelectedStoreService` instead is
 * both the correct tier and a far smaller thing to fake — the pool is keyed by store id and nothing
 * about it is reactive.
 */
class FakeSelectedStore {
  currentSelectedStore() {
    return {id: 'ORG1-STORE1'};
  }
}

describe('ProductSearch', () => {
  let search: ProductSearch;
  let products: FakeProductService;

  beforeEach(() => {
    products = new FakeProductService();

    TestBed.configureTestingModule({
      providers: [
        {provide: ProductService, useValue: products},
        {provide: SelectedStoreService, useValue: new FakeSelectedStore()},
      ],
    });
    search = TestBed.inject(ProductSearch);
  });

  function find(term: string, excludeId: number | null = null): readonly {id: number; name: string}[] {
    let result: readonly {id: number; name: string}[] = [];
    search.find(term, excludeId).subscribe((matches) => (result = matches));
    return result;
  }

  it('finds a product by part of its name', () => {
    /*
     * The whole reason this class exists. `ProductRepository.findAll` reads store, language,
     * availability, SKU, manufacturer and categories — there is no name predicate anywhere in the
     * pod, and `name=` on `tiny-products` is accepted and ignored. Typing a product's name into
     * either picker returned nothing at all until the match moved here.
     */
    expect(find('zoomx').map((product) => product.id)).toEqual([1]);
  });

  it('finds a product by part of its SKU', () => {
    // The old behaviour, kept: an operator with a packing slip in hand searches by code.
    expect(find('ZR-CL').map((product) => product.id)).toEqual([2]);
  });

  it('ignores case', () => {
    expect(find('NIKE').length).toBe(2);
    expect(find('nike').length).toBe(2);
  });

  it('ignores accents, so cafe finds Café', () => {
    expect(find('cafe').map((product) => product.id)).toEqual([3]);
  });

  it('falls back to the SKU for a product with no name in this language', () => {
    // `description` is null on a product not written in the active language; it still has to appear.
    expect(find('XX-NON')[0]?.name).toBe('SKU-XX-NON-005');
  });

  it('offers the head of the catalogue before anything is typed', () => {
    // A picker that shows nothing until you guess a fragment is a picker you cannot browse.
    expect(find('').length).toBeGreaterThan(0);
  });

  it('never offers the product being edited', () => {
    // Nothing relates a product to itself, and offering it is an invitation to a 400.
    expect(find('nike', 1).map((product) => product.id)).toEqual([4]);
  });

  it('reads the catalogue once however many times it is searched', () => {
    find('nike');
    find('zara');
    find('cafe');

    // Without the shared replay every keystroke that outran the debounce re-read the catalogue.
    expect(products.calls).toBe(1);
  });

  it('reads again after a write that could have added a product', () => {
    find('nike');
    search.invalidate();
    find('nike');

    expect(products.calls).toBe(2);
  });
});
