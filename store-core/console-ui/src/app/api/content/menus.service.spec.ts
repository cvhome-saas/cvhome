import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {MenusService} from './menus.service';

describe('MenusService', () => {
  let service: MenusService;
  let http: ReturnType<typeof apiHarness<MenusService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(MenusService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/content/api/v1/private/content/menus';

  it('lists both menus, reads one by handle and replaces its whole tree with PUT', () => {
    service.list().subscribe();
    http.expectOne(scoped(BASE)).flush([]);

    service.get('MAIN').subscribe();
    http.expectOne(scoped(`${BASE}/MAIN`)).flush({handle: 'MAIN', items: []});

    const body = {handle: 'FOOTER', items: []} as never;
    service.put('FOOTER', body).subscribe();
    const replaced = http.expectOne(scoped(`${BASE}/FOOTER`));
    expect(replaced.request.method).toBe('PUT');
    expect(replaced.request.body).toEqual(body);
    replaced.flush({handle: 'FOOTER', items: []});
  });
});
