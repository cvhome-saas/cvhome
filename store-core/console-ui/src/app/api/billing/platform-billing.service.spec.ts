import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {PlatformBillingService} from './platform-billing.service';

/*
 * The api tier's contract with billing's platform half: a path, a verb, a parameter name, the shape
 * of an identifier inside a body. None of it is checked by the compiler — a wrong path is still a
 * string, and a wrongly-shaped id binds to null on the server and *widens* the filter to the whole
 * platform rather than failing. That last one is why the id shapes below are asserted literally.
 */
describe('PlatformBillingService', () => {
  let service: PlatformBillingService;
  let http: ReturnType<typeof apiHarness<PlatformBillingService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(PlatformBillingService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const BASE = '/billing/api/v1/platform';
  const STATISTIC_BASE = '/billing/api/v2/private';
  const ORG = '65f023632bc46470c104b76f';
  const STORE = '507f1f77bcf86cd799439011';

  const emptyPage = {content: [], totalElements: 0, totalPages: 0, size: 20, number: 0};

  /* -------------------------------------------------------------------- the register ---- */

  /*
   * `count`, not Spring's `size`: `store-commons:autoconfigure`'s ServletWebConfig renames the
   * page-size parameter platform-wide, so a `size` would be silently ignored and every page would
   * come back at the server's default. The same assertion `org.service.spec.ts` exists to make.
   */
  it('pages the register with count, not size', () => {
    service.subscriptions({}, 2, 20).subscribe();
    const request = http.expectOne((it) => it.url === `${BASE}/subscriptions`);
    expect(request.request.method).toBe('POST');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('count')).toBe('20');
    expect(request.request.params.has('size')).toBeFalse();
    request.flush(emptyPage);
  });

  /*
   * `ManagerOrgId` is `record ManagerOrgId(ObjectId id)`, so Jackson reads `{"id": "…"}`. A bare
   * string binds the record to null, and a null org filter is *every* organization — a filtered
   * screen that silently shows the whole platform.
   */
  it('wraps the org id, trims the term, and sends nulls for what is not set', () => {
    service.subscriptions({org: ORG, term: '  507f ', status: 'PAST_DUE'}, 0, 20).subscribe();
    const request = http.expectOne((it) => it.url === `${BASE}/subscriptions`);
    expect(request.request.body).toEqual({
      org: {id: ORG},
      status: 'PAST_DUE',
      planCode: null,
      term: '507f',
      blockedOnly: false,
    });
    request.flush(emptyPage);
  });

  it('sends null rather than an empty string for a cleared filter', () => {
    service.subscriptions({org: '', term: '   ', status: '', planCode: ''}, 0, 20).subscribe();
    const request = http.expectOne((it) => it.url === `${BASE}/subscriptions`);
    // A cleared box and an untouched one are one case; two code paths for one behaviour is how they
    // come to disagree.
    expect(request.request.body).toEqual({
      org: null,
      status: null,
      planCode: null,
      term: null,
      blockedOnly: false,
    });
    request.flush(emptyPage);
  });

  /*
   * `blockedOnly` is always a boolean and never null: the server writes it as `:blockedOnly = false`
   * rather than through the nullable-cast idiom, because "blocked" is three statuses and a nullable
   * list cannot be written that way.
   */
  it('always sends blockedOnly as a boolean', () => {
    service.subscriptions({blockedOnly: true}, 0, 20).subscribe();
    const request = http.expectOne((it) => it.url === `${BASE}/subscriptions`);
    expect((request.request.body as {blockedOnly: unknown}).blockedOnly).toBeTrue();
    request.flush(emptyPage);
  });

  /* ---------------------------------------------------------------------- the ledger ---- */

  it('posts the invoice filter with a wrapped org and a bare store id', () => {
    service
      .invoices({org: ORG, store: STORE, status: 'PAID', from: '2026-08-01T00:00:00Z', to: null}, 1, 20)
      .subscribe();
    const request = http.expectOne((it) => it.url === `${BASE}/invoices`);
    // The store is a bare string and the org is wrapped, because `StoreMerchantId` carries
    // `@JsonValue` and `ManagerOrgId` does not. Getting this backwards binds one of them to null.
    expect(request.request.body).toEqual({
      org: {id: ORG},
      store: STORE,
      status: 'PAID',
      from: '2026-08-01T00:00:00Z',
      to: null,
    });
    expect(request.request.params.get('count')).toBe('20');
    request.flush(emptyPage);
  });

  /*
   * The totals are a second call on the *same* body. A sum computed over a wider filter than the
   * rows on screen is worse than no sum: it looks authoritative.
   */
  it('sums the ledger over exactly the filter the rows were read with', () => {
    const filter = {org: ORG, status: 'OPEN'} as const;
    service.invoices(filter, 0, 20).subscribe();
    const rows = http.expectOne((it) => it.url === `${BASE}/invoices`);
    service.invoiceTotals(filter).subscribe();
    const totals = http.expectOne((it) => it.url === `${BASE}/invoices/totals`);

    expect(totals.request.method).toBe('POST');
    expect(totals.request.body).toEqual(rows.request.body);
    // The totals are not paged: they are one figure per currency, however many rows they cover.
    expect(totals.request.params.has('page')).toBeFalse();
    rows.flush(emptyPage);
    totals.flush([]);
  });

  /* -------------------------------------------------------------------- the audit trail ---- */

  it('posts the audit filter with a bare store id and a wrapped org', () => {
    service
      .audit({store: STORE, org: ORG, eventType: 'PLAN_UPGRADED', source: 'API', from: null, to: null}, 0, 50)
      .subscribe();
    const request = http.expectOne((it) => it.url === `${BASE}/audit`);
    expect(request.request.body).toEqual({
      store: STORE,
      org: {id: ORG},
      eventType: 'PLAN_UPGRADED',
      source: 'API',
      from: null,
      to: null,
    });
    expect(request.request.params.get('count')).toBe('50');
    request.flush(emptyPage);
  });

  /* ------------------------------------------------------------------- the aggregates ---- */

  it('reads the plan statistics as a GET on the v2 base', () => {
    service.planStatistics().subscribe();
    // `/api/v2`, matching tenancy's counters — and a GET, because it is a reading of the present
    // rather than of a period.
    const request = http.expectOne((it) => it.url === `${STATISTIC_BASE}/plan-statistic`);
    expect(request.request.method).toBe('GET');
    request.flush({counts: [], recurringValue: []});
  });

  it('reads billing health as a GET', () => {
    service.health().subscribe();
    const request = http.expectOne((it) => it.url === `${STATISTIC_BASE}/billing-health`);
    expect(request.request.method).toBe('GET');
    request.flush({failedEvents: 0, stalledRequests: 0, staleAfterMinutes: 10});
  });

  /*
   * Every request carries the request context's `?store=`, whether or not the endpoint reads it —
   * these do not, but `CrudService` stamps it on everything and a service that stopped going through
   * it would silently lose its tenant scope elsewhere.
   */
  it('goes through CrudService, so the request context is still stamped', () => {
    service.subscriptions({}, 0, 20).subscribe();
    const request = http.expectOne((it) => it.url === `${BASE}/subscriptions`);
    expect(request.request.params.get('store')).toBe(TEST_STORE);
    request.flush(emptyPage);
  });
});
