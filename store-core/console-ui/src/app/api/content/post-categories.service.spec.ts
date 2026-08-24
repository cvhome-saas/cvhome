import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {PostCategoriesService} from './post-categories.service';

describe('PostCategoriesService', () => {
  let service: PostCategoriesService;
  let http: ReturnType<typeof apiHarness<PostCategoriesService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(PostCategoriesService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/content/api/v1/private/content/posts/categories';

  it('lists, creates, updates and deletes categories', () => {
    service.list().subscribe();
    http.expectOne(scoped(BASE)).flush([]);

    service.create({name: 'News'} as never).subscribe();
    const created = http.expectOne(scoped(BASE));
    expect(created.request.method).toBe('POST');
    created.flush({id: 7, name: 'News'});

    service.update(7, {name: 'Updates'} as never).subscribe();
    const updated = http.expectOne(scoped(`${BASE}/7`));
    expect(updated.request.method).toBe('PUT');
    updated.flush({id: 7, name: 'Updates'});

    service.delete(7).subscribe();
    const deleted = http.expectOne(scoped(`${BASE}/7`));
    expect(deleted.request.method).toBe('DELETE');
    deleted.flush(null);
  });
});
