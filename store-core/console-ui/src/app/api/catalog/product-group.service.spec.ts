import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {ProductGroupService} from './product-group.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string — and this tier had one spec for
 * twenty-four files.
 */
describe('ProductGroupService', () => {
  let service: ProductGroupService;
  let http: ReturnType<typeof apiHarness<ProductGroupService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(ProductGroupService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const page = {content: [], size: 0, totalElements: 0, totalPages: 0, pageNumber: 0};
  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/catalog/api/v1';

  /*
   * A group is addressed by its *code*, not an id — and the list answers `products: []` for every
   * group whatever they contain, which is why the tab reads members from the by-code call. See
   * lessons.md, "Catalogue — the second QA pass, and why the first one missed it".
   */
  it('lists groups and reads one by its code', () => {
    service.list({page: 0, count: 500}).subscribe();
    http.expectOne((candidate) => candidate.url === `${BASE}/private/products/groups`).flush(page);

    service.get('SUMMER').subscribe();
    http.expectOne(scoped(`${BASE}/private/products/groups/SUMMER`)).flush({} as never);
  });

  it('adds and removes a member by product id under the group’s code', () => {
    service.addProduct('SUMMER', 7).subscribe();
    const added = http.expectOne(scoped(`${BASE}/private/products/groups/SUMMER/product/7`));
    expect(added.request.method).toBe('POST');
    added.flush(null);

    service.removeProduct('SUMMER', 7).subscribe();
    const removed = http.expectOne(scoped(`${BASE}/private/products/groups/SUMMER/product/7`));
    expect(removed.request.method).toBe('DELETE');
    removed.flush(null);
  });
});
