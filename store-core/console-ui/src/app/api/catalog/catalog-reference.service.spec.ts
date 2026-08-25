import {SelectedStoreService} from '@api/tenancy/selected-store.service';
import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {CatalogReference} from './catalog-reference.service';

const HIERARCHY = '/spg/catalog/api/v1/private/category-hierarchy';
const BRANDS = '/spg/catalog/api/v1/private/manufacturers';

/** The open store, which is what the cache is keyed on. Switchable, so a switch can be exercised. */
class FakeSelectedStore {
  id: string | null = TEST_STORE;

  currentSelectedStore(): {id: string} | null {
    return this.id === null ? null : {id: this.id};
  }
}

describe('CatalogReference', () => {
  let reference: CatalogReference;
  let http: ReturnType<typeof apiHarness<CatalogReference>>['http'];
  let selected: FakeSelectedStore;

  beforeEach(() => {
    selected = new FakeSelectedStore();
    const harness = apiHarness(CatalogReference, [
      {provide: SelectedStoreService, useValue: selected},
    ]);
    reference = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const page = <T>(content: T[]) => ({
    content,
    size: content.length,
    totalElements: content.length,
    totalPages: 1,
    pageNumber: 0,
  });

  it('reads a reference list once and shares it', () => {
    reference.hierarchy().subscribe();
    reference.hierarchy().subscribe();

    const request = http.expectOne((candidate) => candidate.url === HIERARCHY);
    request.flush(page([{id: 1}]));

    // Two readers, one round trip: this is the whole reason the service exists — moving between
    // the products list and a product re-read all four lists every time without it.
    reference.hierarchy().subscribe();
    http.expectNone((candidate) => candidate.url === HIERARCHY);
  });

  it('asks for a page large enough that no store’s taxonomy pages', () => {
    reference.brandList().subscribe();
    const request = http.expectOne((candidate) => candidate.url === BRANDS);

    expect(request.request.params.get('count')).toBe('500');
    expect(request.request.params.get('page')).toBe('0');
    request.flush(page([]));
  });

  it('scopes the request to the open store, through the request context', () => {
    reference.brandList().subscribe();
    const request = http.expectOne((candidate) => candidate.url === BRANDS);

    expect(request.request.params.get('store')).toBe(TEST_STORE);
    request.flush(page([]));
  });

  it('reads again for a different store, rather than serving the first one’s answer', () => {
    reference.brandList().subscribe();
    http.expectOne((candidate) => candidate.url === BRANDS).flush(page([{id: 1}]));

    selected.id = 'ORG1-STORE2';
    reference.brandList().subscribe();

    http.expectOne((candidate) => candidate.url === BRANDS).flush(page([{id: 2}]));
  });

  /*
   * `shareReplay` remembers an error as faithfully as a value, so without the eviction a single
   * 404 — a service still warming up, a network blip — is replayed to every reader for the rest of
   * the session, and the brand list stays missing until a catalogue write happens to clear it.
   */
  it('does not cache a failure', () => {
    reference.brandList().subscribe({error: () => undefined});
    http.expectOne((candidate) => candidate.url === BRANDS)
      .flush({code: 'X'}, {status: 503, statusText: 'Service Unavailable'});

    reference.brandList().subscribe({error: () => undefined});

    http.expectOne((candidate) => candidate.url === BRANDS).flush(page([]));
  });

  it('goes back to the server after a write invalidates it', () => {
    reference.brandList().subscribe();
    http.expectOne((candidate) => candidate.url === BRANDS).flush(page([{id: 1}]));

    reference.invalidate();
    reference.brandList().subscribe();

    http.expectOne((candidate) => candidate.url === BRANDS).flush(page([{id: 1}]));
  });
});
