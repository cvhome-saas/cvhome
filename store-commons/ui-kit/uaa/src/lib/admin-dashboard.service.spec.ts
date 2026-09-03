import {apiHarness, verifyNoPendingRequests} from '@cvhome-saas/ui-kit';
import {AdminDashboardService} from './admin-dashboard.service';

/** One read, one parameter. */
describe('AdminDashboardService', () => {
  let service: AdminDashboardService;
  let http: ReturnType<typeof apiHarness<AdminDashboardService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(AdminDashboardService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  it('defaults to the last 24 hours and passes a chosen range', () => {
    service.get().subscribe();
    expect(http.expectOne((it) => it.url.endsWith('/uaa/api/v1/admin/dashboard')).request.params.get('range')).toBe('24h');
    http.verify();

    service.get('7d').subscribe();
    const week = http.expectOne((it) => it.url.endsWith('/uaa/api/v1/admin/dashboard'));
    expect(week.request.params.get('range')).toBe('7d');
    week.flush({range: '7d'});
  });
});
