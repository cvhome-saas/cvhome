import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {ProductVariantService} from './product-variant.service';

describe('ProductVariantService', () => {
  let service: ProductVariantService;
  let http: ReturnType<typeof apiHarness<ProductVariantService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(ProductVariantService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/catalog/api/v2';

  it('reads the matrix and replaces the whole set atomically', () => {
    service.list(12).subscribe();
    http.expectOne(scoped(`${BASE}/private/product/12/variants`)).flush([]);

    service
      .replace(12, {
        options: ['color'],
        variants: [{sku: 'SHIRT-RED', defaultVariant: true, optionValueIds: [71]}],
      })
      .subscribe();
    const replace = http.expectOne(scoped(`${BASE}/private/product/12/variants`));
    expect(replace.request.method).toBe('PUT');
    // The axes and the combinations travel together — the whole point of the atomic PUT.
    expect(replace.request.body.options).toEqual(['color']);
    expect(replace.request.body.variants[0].sku).toBe('SHIRT-RED');
    replace.flush(null);
  });
});
