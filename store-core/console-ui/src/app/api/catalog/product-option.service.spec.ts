import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {ProductOptionService} from './product-option.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string.
 */
describe('ProductOptionService', () => {
  let service: ProductOptionService;
  let http: ReturnType<typeof apiHarness<ProductOptionService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(ProductOptionService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const page = {content: [], size: 0, totalElements: 0, totalPages: 0, pageNumber: 0};
  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/catalog/api/v1';

  it('lists, reads, writes and deletes an option', () => {
    service.list({page: 0, count: 500}).subscribe();
    http.expectOne((candidate) => candidate.url === `${BASE}/private/product/options`).flush(page);

    service.get(7).subscribe();
    http.expectOne(scoped(`${BASE}/private/product/option/7`)).flush({} as never);

    service.create({code: 'color', descriptions: [], values: []}).subscribe();
    const create = http.expectOne(scoped(`${BASE}/private/product/option`));
    expect(create.request.method).toBe('POST');
    create.flush({id: 7});

    service.update(7, {code: 'color', descriptions: [], values: []}).subscribe();
    const update = http.expectOne(scoped(`${BASE}/private/product/option/7`));
    expect(update.request.method).toBe('PUT');
    update.flush(null);

    service.delete(7).subscribe();
    const remove = http.expectOne(scoped(`${BASE}/private/product/option/7`));
    expect(remove.request.method).toBe('DELETE');
    remove.flush(null);
  });

  it('probes code uniqueness on the singular path', () => {
    service.codeTaken('color').subscribe();
    const check = http.expectOne(
      (candidate) => candidate.url === `${BASE}/private/product/option/unique`,
    );
    expect(check.request.params.get('code')).toBe('color');
    check.flush({exists: false});
  });
});
