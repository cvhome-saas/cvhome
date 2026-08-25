import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {ProductService} from './product.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape.
 * None of it is checked by the compiler — a wrong path is still a string — and this tier had one
 * spec for twenty-four files. A contract change is a red test here rather than a QA finding two
 * modules later.
 */
describe('ProductService', () => {
  let service: ProductService;
  let http: ReturnType<typeof apiHarness<ProductService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(ProductService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const V1 = '/spg/catalog/api/v1';
  const V2 = '/spg/catalog/api/v2';

  const page = {content: [], size: 0, totalElements: 0, totalPages: 0, pageNumber: 0};

  /*
   * The public list, not `private/base-products`. That one answers `description: null`,
   * `categories: []` and `manufacturer: null` on every row — see lessons.md, "Catalogue — the
   * private product list is stripped of everything a list needs".
   */
  it('lists from the endpoint that actually fills a row', () => {
    service.list({page: 0, count: 20, sku: 'ACM'}).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${V2}/products`);

    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('sku')).toBe('ACM');
    expect(request.request.params.get('store')).toBe(TEST_STORE);
    request.flush(page);
  });

  it('reads, creates and updates a definition on v2', () => {
    service.definition(7).subscribe();
    http.expectOne(`${V2}/private/product/7?store=${TEST_STORE}`).flush({} as never);

    service.create({sku: 'NEW'} as never).subscribe();
    const created = http.expectOne(`${V2}/private/product?store=${TEST_STORE}`);
    expect(created.request.method).toBe('POST');
    created.flush({id: 7});

    service.update(7, {sku: 'NEW'} as never).subscribe();
    const updated = http.expectOne(`${V2}/private/product/7?store=${TEST_STORE}`);
    expect(updated.request.method).toBe('PUT');
    updated.flush(null);
  });

  /* The inline edit is a v1 PATCH, and the version difference is the contract, not an accident. */
  it('patches and deletes on v1', () => {
    service.patch(7, {price: 12} as never).subscribe();
    const patched = http.expectOne(`${V1}/private/product/7?store=${TEST_STORE}`);
    expect(patched.request.method).toBe('PATCH');
    patched.flush(null);

    service.delete(7).subscribe();
    const deleted = http.expectOne(`${V1}/private/product/7?store=${TEST_STORE}`);
    expect(deleted.request.method).toBe('DELETE');
    deleted.flush(null);
  });

  it('asks whether a SKU is taken', () => {
    service.skuTaken('ACM-1').subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${V1}/private/product/unique`);
    expect(request.request.params.get('code')).toBe('ACM-1');
    request.flush({exists: false});
  });

  /*
   * seller-core built this path with a literal trailing brace, so it never matched its mapping and
   * assigning a category has never worked from the old console. See lessons.md, "Catalogue — two
   * seller-core calls have never worked".
   */
  it('assigns and unassigns a category on a path with no stray brace in it', () => {
    service.addToCategory(7, 3).subscribe();
    const added = http.expectOne(`${V1}/private/product/7/category/3?store=${TEST_STORE}`);
    expect(added.request.method).toBe('POST');
    added.flush(null);

    service.removeFromCategory(7, 3).subscribe();
    const removed = http.expectOne(`${V1}/private/product/7/category/3?store=${TEST_STORE}`);
    expect(removed.request.method).toBe('DELETE');
    removed.flush(null);
  });
});
