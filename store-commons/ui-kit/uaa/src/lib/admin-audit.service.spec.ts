import {apiHarness, verifyNoPendingRequests} from '@cvhome-saas/ui-kit';
import {AdminAuditService} from './admin-audit.service';

/** The filters are a query string; the export is an address the browser follows itself. */
describe('AdminAuditService', () => {
  let service: AdminAuditService;
  let http: ReturnType<typeof apiHarness<AdminAuditService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(AdminAuditService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const BASE = '/uaa/api/v1/admin/audit';

  it('pages the newest events', () => {
    service.search(2, 25).subscribe();
    const request = http.expectOne((it) => it.url === BASE);
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('25');
    request.flush({content: [], totalElements: 0});
  });

  it('joins lists with commas and sends only the filters that are set', () => {
    service.search(0, 50, {type: ['user.login', 'user.login.failed'], category: ['SECURITY'], q: ' reset ', actor: null}).subscribe();
    const request = http.expectOne((it) => it.url === BASE);
    expect(request.request.params.get('type')).toBe('user.login,user.login.failed');
    expect(request.request.params.get('category')).toBe('SECURITY');
    expect(request.request.params.get('q')).toBe('reset');
    expect(request.request.params.has('actor')).toBe(false);
    request.flush({content: [], totalElements: 0});
  });

  it('reads one event and the type catalogue', () => {
    let event: {id: number} | undefined;
    service.findOne(7).subscribe((it) => (event = it as {id: number}));
    http.expectOne((it) => it.url === `${BASE}/7`).flush({id: 7});
    expect(event?.id).toBe(7);

    let types: readonly {type: string}[] = [];
    service.types().subscribe((it) => (types = it));
    http.expectOne((it) => it.url === `${BASE}/types`).flush([{type: 'user.login', category: 'AUTHENTICATION'}]);
    expect(types.map((it) => it.type)).toEqual(['user.login']);
  });

  it('builds the export address without fetching it', () => {
    expect(service.exportUrl()).toContain(`${BASE}/export`);
    expect(service.exportUrl({category: ['ADMIN'], outcome: 'FAILURE'})).toContain('category=ADMIN&outcome=FAILURE');
  });
});
