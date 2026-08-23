import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {ContentItemsService} from './content-items.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string.
 */
describe('ContentItemsService', () => {
  let service: ContentItemsService;
  let http: ReturnType<typeof apiHarness<ContentItemsService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(ContentItemsService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const BASE = '/spg/content/api/v1/private/content';

  it('lists a type with the filters as query params and `count` as the page size', () => {
    service
      .list('pages', {status: 'PUBLISHED', locale: 'ar', state: 'MISSING', q: 'about'}, {page: 2, count: 25})
      .subscribe();
    const request = http.expectOne(
      (r) => r.url === `${BASE}/pages` && r.params.get('status') === 'PUBLISHED' && r.params.get('locale') === 'ar',
    );
    expect(request.request.params.get('state')).toBe('MISSING');
    expect(request.request.params.get('q')).toBe('about');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('count')).toBe('25');
    expect(request.request.params.get('store')).toBe(TEST_STORE);
    request.flush({content: [], totalElements: 0, totalPages: 0, pageNumber: 0, size: 25});
  });

  it('reads, creates, updates and deletes under the type segment', () => {
    service.get('posts', 7).subscribe();
    http.expectOne((r) => r.url === `${BASE}/posts/7` && r.method === 'GET').flush({});

    service.create('posts', {slug: 'hello', translations: []}).subscribe();
    const created = http.expectOne((r) => r.url === `${BASE}/posts`);
    expect(created.request.method).toBe('POST');
    created.flush({id: 7, status: 'DRAFT', version: 0});

    service.update('posts', 7, {slug: 'hello', translations: [], version: 0}).subscribe();
    const updated = http.expectOne((r) => r.url === `${BASE}/posts/7` && r.method === 'PUT');
    expect(updated.request.body.version).toBe(0);
    updated.flush({id: 7, status: 'DRAFT', version: 1});

    service.delete('posts', 7, true).subscribe();
    const deleted = http.expectOne((r) => r.url === `${BASE}/posts/7` && r.method === 'DELETE');
    expect(deleted.request.params.get('force')).toBe('true');
    deleted.flush(null);
  });

  it('runs a transition as POST …/{id}/{action}, with the window when publishing', () => {
    service.transition('pages', 3, 'publish', {publishAt: '2026-12-01T09:00:00Z'}).subscribe();
    const request = http.expectOne((r) => r.url === `${BASE}/pages/3/publish`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body.publishAt).toBe('2026-12-01T09:00:00Z');
    request.flush({id: 3, status: 'SCHEDULED', version: 2});

    service.transition('pages', 3, 'unpublish').subscribe();
    http.expectOne((r) => r.url === `${BASE}/pages/3/unpublish`).flush({id: 3, status: 'DRAFT', version: 3});
  });

  it('asks whether a slug is free, excluding the item being edited', () => {
    service.slugAvailable('pages', 'about-us', 3).subscribe();
    const request = http.expectOne((r) => r.url === `${BASE}/pages/slug-available`);
    expect(request.request.params.get('slug')).toBe('about-us');
    expect(request.request.params.get('excludeId')).toBe('3');
    request.flush({exists: true});
  });

  it('posts a bulk action with the ids', () => {
    service.bulk('faq', [1, 2], 'ARCHIVE').subscribe();
    const request = http.expectOne((r) => r.url === `${BASE}/faq/bulk`);
    expect(request.request.body).toEqual({ids: [1, 2], action: 'ARCHIVE'});
    request.flush([]);
  });
});
