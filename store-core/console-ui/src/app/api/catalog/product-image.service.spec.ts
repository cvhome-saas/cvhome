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
   * `image`, singular. seller-core posts to `/images` and the pod maps no such path, so uploading
   * has never worked from the old console — see lessons.md, "Catalogue — two seller-core calls have
   * never worked". `HttpClient` sets the multipart boundary from the `FormData`; a hand-set
   * `Content-Type` would break it, so there must not be one.
   */
  it('uploads multipart to the singular path, with the order and default flag as parameters', () => {
    const file = new File(['x'], 'shot.png', {type: 'image/png'});
    service.upload(7, file, 2, true).subscribe();

    const request = http.expectOne((candidate) => candidate.url === `${BASE}/private/product/7/image`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body instanceof FormData).toBeTrue();
    expect(request.request.headers.has('Content-Type')).toBeFalse();
    expect(request.request.params.get('order')).toBe('2');
    expect(request.request.params.get('defaultImage')).toBe('true');
    request.flush(null);
  });

  it('reorders with a PATCH whose whole request is the order parameter', () => {
    service.reorder(7, 3, 1).subscribe();
    const request = http.expectOne((candidate) =>
      candidate.url === `${BASE}/private/product/7/image/3`);
    expect(request.request.method).toBe('PATCH');
    expect(request.request.params.get('order')).toBe('1');
    request.flush(null);
  });

  it('removes an image by id', () => {
    service.remove(7, 3).subscribe();
    const request = http.expectOne(scoped(`${BASE}/private/product/7/image/3`));
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
