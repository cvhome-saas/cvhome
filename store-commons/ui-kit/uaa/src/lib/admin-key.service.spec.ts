import {apiHarness, verifyNoPendingRequests} from '@cvhome-saas/ui-kit';
import {AdminKeyService} from './admin-key.service';

/** Three reads and one empty POST; nothing here ever carries key material. */
describe('AdminKeyService', () => {
  let service: AdminKeyService;
  let http: ReturnType<typeof apiHarness<AdminKeyService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(AdminKeyService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const BASE = '/uaa/api/v1/admin/keys';

  it('lists, reads the status and rotates', () => {
    service.list().subscribe();
    http.expectOne((it) => it.url === BASE).flush([]);

    service.status().subscribe();
    http.expectOne((it) => it.url === `${BASE}/status`).flush({activeKid: 'k'});

    service.rotate().subscribe();
    const rotate = http.expectOne((it) => it.url === `${BASE}/rotate`);
    expect(rotate.request.method).toBe('POST');
    rotate.flush({id: '1', kid: 'k2', algorithm: 'RS256', status: 'ACTIVE'});
  });
});
