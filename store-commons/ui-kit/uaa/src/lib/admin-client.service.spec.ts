import {apiHarness, verifyNoPendingRequests} from '@cvhome-saas/ui-kit';
import {AdminClientService} from './admin-client.service';

/** The registry's contract: filters as plain parameters, the secret-bearing writes as empty POSTs on the client's path. */
describe('AdminClientService', () => {
  let service: AdminClientService;
  let http: ReturnType<typeof apiHarness<AdminClientService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(AdminClientService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const BASE = '/uaa/api/v1/admin/clients';
  const ID = 'a5c7e2c0-7e7e-8f5f-c2d2-7e7e8f5fc2d2';
  const emptyPage = {content: [], totalElements: 0, totalPages: 0, size: 20, number: 0};

  it('sends the list filters, and only the ones that are set', () => {
    service.list(0, 20, {q: ' store ', enabled: false, type: 'MACHINE'}).subscribe();
    const request = http.expectOne((it) => it.url === BASE);
    expect(request.request.params.get('q')).toBe('store');
    expect(request.request.params.get('enabled')).toBe('false');
    expect(request.request.params.get('type')).toBe('MACHINE');
    request.flush(emptyPage);

    service.list(0, 20, {q: '', enabled: null, type: ''}).subscribe();
    const bare = http.expectOne((it) => it.url === BASE);
    expect(bare.request.params.has('q')).toBeFalse();
    expect(bare.request.params.has('enabled')).toBeFalse();
    expect(bare.request.params.has('type')).toBeFalse();
    bare.flush(emptyPage);
  });

  it('rotates, revokes the previous secret, enables and disables on the client path', () => {
    service.rotateSecret(ID).subscribe();
    const rotate = http.expectOne((it) => it.url === `${BASE}/${ID}/rotate-secret`);
    expect(rotate.request.method).toBe('POST');
    rotate.flush({id: ID, clientId: 'admin-sdk', clientSecret: 's', clientSecretExpiresAt: null, previousSecretUntil: null});

    service.revokePreviousSecret(ID).subscribe();
    const revoke = http.expectOne((it) => it.url === `${BASE}/${ID}/previous-secret`);
    expect(revoke.request.method).toBe('DELETE');
    revoke.flush(null);

    service.disable(ID).subscribe();
    http.expectOne((it) => it.url === `${BASE}/${ID}/disable`).flush({});
    service.enable(ID).subscribe();
    http.expectOne((it) => it.url === `${BASE}/${ID}/enable`).flush({});
  });

  it('reads the stats and rotates everything from the collection path', () => {
    service.stats().subscribe();
    http.expectOne((it) => it.url === `${BASE}/stats`).flush({total: 4});

    service.rotateAll().subscribe();
    const all = http.expectOne((it) => it.url === `${BASE}/rotate-all`);
    expect(all.request.method).toBe('POST');
    all.flush([]);
  });
});
