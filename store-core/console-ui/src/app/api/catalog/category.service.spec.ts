import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {CategoryService} from './category.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string — and this tier had one spec for
 * twenty-four files.
 */
describe('CategoryService', () => {
  let service: CategoryService;
  let http: ReturnType<typeof apiHarness<CategoryService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(CategoryService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const page = {content: [], size: 0, totalElements: 0, totalPages: 0, pageNumber: 0};
  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/catalog/api/v1';

  it('reads the hierarchy and the flat list from different paths', () => {
    service.hierarchy({page: 0, count: 500}).subscribe();
    const tree = http.expectOne((candidate) => candidate.url === `${BASE}/private/category-hierarchy`);
    expect(tree.request.params.get('count')).toBe('500');
    tree.flush(page);

    service.list({page: 0, count: 20}).subscribe();
    http.expectOne((candidate) => candidate.url === `${BASE}/private/category`).flush(page);
  });

  it('reads one category, and the categories a product is in', () => {
    service.get(3).subscribe();
    http.expectOne(scoped(`${BASE}/private/category/3`)).flush({} as never);

    service.forProduct(7).subscribe();
    http.expectOne(scoped(`${BASE}/private/category/product/7`)).flush(page);
  });

  it('creates with a POST and updates with a PUT on the id', () => {
    service.create({code: 'shoes'} as never).subscribe();
    const created = http.expectOne(scoped(`${BASE}/private/category`));
    expect(created.request.method).toBe('POST');
    created.flush({} as never);

    service.update(3, {code: 'shoes'} as never).subscribe();
    const updated = http.expectOne(scoped(`${BASE}/private/category/3`));
    expect(updated.request.method).toBe('PUT');
    updated.flush({} as never);
  });
});
