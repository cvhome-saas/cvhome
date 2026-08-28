import {TEST_STORE, apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import {ProductImageService} from './product-image.service';

/*
 * The api tier's contract with the backend: a path, a verb, a parameter name, a body shape. None of
 * it is checked by the compiler — a wrong path is still a string.
 */
describe('ProductImageService', () => {
  let service: ProductImageService;
  let http: ReturnType<typeof apiHarness<ProductImageService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(ProductImageService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const scoped = (path: string) => `${path}?store=${TEST_STORE}`;

  const BASE = '/spg/catalog/api/v1';

  it('reads a product’s images from the public path', () => {
    service.images(7).subscribe();
    http.expectOne(scoped(`${BASE}/product/7/images`)).flush([]);
  });

  /*
   * Asset ids, not bytes. The library holds the file; catalog holds the reference, which is what
   * lets an image carry alt text, be reused across products and be protected from deletion.
   */
  it('attaches library assets as JSON', () => {
    service.attach(7, [{mediaAssetId: 11, altText: 'Front'}]).subscribe();

    const request = http.expectOne((candidate) => candidate.url === `${BASE}/private/product/7/images`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual([{mediaAssetId: 11, altText: 'Front'}]);
    request.flush([]);
  });

  /*
   * The whole list in one PUT. Reordering used to be one PATCH per image, which left two images
   * sharing a position when a call in the middle failed, and could not set the default at all.
   */
  it('replaces the whole gallery, order being the list order', () => {
    service.replace(7, [{mediaAssetId: 22, defaultImage: true}, {mediaAssetId: 11}]).subscribe();

    const request = http.expectOne((candidate) => candidate.url === `${BASE}/private/product/7/images`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual([{mediaAssetId: 22, defaultImage: true}, {mediaAssetId: 11}]);
    request.flush([]);
  });

  it('removes an image by id', () => {
    service.remove(7, 3).subscribe();
    const request = http.expectOne(scoped(`${BASE}/private/product/7/image/3`));
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
