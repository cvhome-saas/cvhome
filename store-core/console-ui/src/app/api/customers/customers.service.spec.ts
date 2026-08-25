import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {CustomersService} from './customers.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name. None of it is checked
 * by the compiler — a wrong path is still a string.
 *
 * The filter parameters matter more here than usual: they were implemented in the repository and
 * bound to nothing for the life of the endpoint, so a regression that dropped them again would look
 * exactly like the list simply not narrowing.
 */
describe('CustomersService', () => {
  let service: CustomersService;
  let http: ReturnType<typeof apiHarness<CustomersService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(CustomersService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const BASE = '/spg/checkout/api/v1';

  it('reads the paged list with the store stamped on it', () => {
    service.list({page: 1, count: 20}).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${BASE}/private/customers`);

    expect(request.request.method).toBe('GET');
    // `count`, not `size`: `ServletWebConfig` names the page-size parameter platform-wide.
    expect(request.request.params.get('count')).toBe('20');
    expect(request.request.params.get('page')).toBe('1');
    expect(request.request.params.get('store')).toBe(TEST_STORE);
    request.flush({content: [], size: 20, totalElements: 0, totalPages: 0, pageNumber: 1});
  });

  it('sends the search term as the one parameter that spans name and email', () => {
    service.list({page: 0, count: 20, name: 'marta'}).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${BASE}/private/customers`);

    expect(request.request.params.get('name')).toBe('marta');
    // Not also as `email`: the server ANDs its filters, so sending both would match nothing.
    expect(request.request.params.has('email')).toBe(false);
    request.flush({content: [], size: 20, totalElements: 0, totalPages: 0, pageNumber: 0});
  });

  it('sends the exact-field filters when it is given them', () => {
    service.list({page: 0, count: 20, email: 'marta@nordwerk.pl', country: 'PL'}).subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${BASE}/private/customers`);

    expect(request.request.params.get('email')).toBe('marta@nordwerk.pl');
    expect(request.request.params.get('country')).toBe('PL');
    request.flush({content: [], size: 20, totalElements: 0, totalPages: 0, pageNumber: 0});
  });
});
