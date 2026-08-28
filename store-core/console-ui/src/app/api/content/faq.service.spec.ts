import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {FaqService} from './faq.service';

describe('FaqService', () => {
  let service: FaqService;
  let http: ReturnType<typeof apiHarness<FaqService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(FaqService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/content/api/v1/private/content/faq';

  it('lists, creates, updates and deletes groups', () => {
    service.groups().subscribe();
    http.expectOne(scoped(`${BASE}/groups`)).flush([]);

    service.createGroup({name: 'Shipping'} as never).subscribe();
    const created = http.expectOne(scoped(`${BASE}/groups`));
    expect(created.request.method).toBe('POST');
    created.flush({id: 1, name: 'Shipping'});

    service.updateGroup(1, {name: 'Delivery'} as never).subscribe();
    const updated = http.expectOne(scoped(`${BASE}/groups/1`));
    expect(updated.request.method).toBe('PUT');
    updated.flush({id: 1, name: 'Delivery'});

    service.deleteGroup(1).subscribe();
    const deleted = http.expectOne(scoped(`${BASE}/groups/1`));
    expect(deleted.request.method).toBe('DELETE');
    deleted.flush(null);
  });

  it('reorders entries in one PATCH carrying every move', () => {
    const moves = [
      {entryId: 5, groupId: 1, position: 0},
      {entryId: 6, groupId: 2, position: 1},
    ];
    service.reorder(moves as never).subscribe();
    const request = http.expectOne(scoped(`${BASE}/reorder`));
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual(moves);
    request.flush(null);
  });
});
