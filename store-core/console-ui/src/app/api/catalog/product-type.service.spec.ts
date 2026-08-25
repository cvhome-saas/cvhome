import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {ProductTypeService} from './product-type.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string — and this tier had one spec for
 * twenty-four files.
 */
describe('ProductTypeService', () => {
  let service: ProductTypeService;
  let http: ReturnType<typeof apiHarness<ProductTypeService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(ProductTypeService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const page = {content: [], size: 0, totalElements: 0, totalPages: 0, pageNumber: 0};
  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/catalog/api/v1';

  it('lists, reads, writes and deletes a product type', () => {
    service.list({page: 0, count: 500}).subscribe();
    http.expectOne((candidate) => candidate.url === `${BASE}/private/product/types`).flush(page);

    service.get(5).subscribe();
    http.expectOne(scoped(`${BASE}/private/product/type/5`)).flush({} as never);

    service.create({code: 'shoe'} as never).subscribe();
    expect(http.expectOne(scoped(`${BASE}/private/product/type`)).request.method).toBe('POST');

    service.update(5, {code: 'shoe'} as never).subscribe();
    expect(http.expectOne(scoped(`${BASE}/private/product/type/5`)).request.method).toBe('PUT');

    service.delete(5).subscribe();
    expect(http.expectOne(scoped(`${BASE}/private/product/type/5`)).request.method).toBe('DELETE');
  });
});
