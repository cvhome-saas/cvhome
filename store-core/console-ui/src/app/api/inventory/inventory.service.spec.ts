import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {InventoryService} from './inventory.service';

describe('InventoryService', () => {
  let service: InventoryService;
  let http: ReturnType<typeof apiHarness<InventoryService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(InventoryService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/inventory/api/v1';

  it('reads availability for a set of skus in one call', () => {
    service.bySkus(['A-1', 'B-2']).subscribe();
    const read = http.expectOne((candidate) => candidate.url === `${BASE}/availability`);
    expect(read.request.params.get('skus')).toBe('A-1,B-2');
    read.flush([]);
  });

  it('bulk-upserts a matrix as one wrapped batch', () => {
    service
      .bulkUpsert([
        {sku: 'A-1', inventory: {quantity: 3, available: true, price: {amount: 10}}},
      ])
      .subscribe();
    const bulk = http.expectOne(scoped(`${BASE}/private/inventory/bulk`));
    expect(bulk.request.method).toBe('PUT');
    // The record wrapper, not a naked list — Spring 6.1 turns a bare list's validation into a 500.
    expect(bulk.request.body.entries[0].sku).toBe('A-1');
    bulk.flush([]);
  });

  it('deletes one sku, url-encoded', () => {
    service.deleteBySku('A/1').subscribe();
    const remove = http.expectOne(scoped(`${BASE}/private/inventory/A%2F1`));
    expect(remove.request.method).toBe('DELETE');
    remove.flush(null);
  });
});
