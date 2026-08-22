import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {ProductRelationshipService} from './product-relationship.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string — and this tier had one spec for
 * twenty-four files.
 */
describe('ProductRelationshipService', () => {
  let service: ProductRelationshipService;
  let http: ReturnType<typeof apiHarness<ProductRelationshipService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(ProductRelationshipService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/catalog/api/v1';

  it('reads a product’s related products, and attaches and detaches one', () => {
    service.related(7).subscribe();
    http.expectOne(scoped(`${BASE}/products/7/relationship`)).flush({} as never);

    service.add(7, 9).subscribe();
    const added = http.expectOne(scoped(`${BASE}/private/products/7/relationship/9`));
    expect(added.request.method).toBe('POST');
    added.flush(null);

    service.remove(7, 9).subscribe();
    const removed = http.expectOne(scoped(`${BASE}/private/products/7/relationship/9`));
    expect(removed.request.method).toBe('DELETE');
    removed.flush(null);
  });
});
