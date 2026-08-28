import {HttpErrorResponse} from '@angular/common/http';
import {ApiError} from './api-error';
import {normalizeFieldPath, statusToCategory, toApiError} from './problem-detail.parser';
import {ProblemDetail} from './problem-detail.model';

const URL = 'https://gateway.com:8000/spg/catalog/api/v1/products';

function httpError(init: {status: number; error?: unknown; url?: string}): HttpErrorResponse {
  return new HttpErrorResponse({status: init.status, error: init.error, url: init.url ?? URL});
}

const PROBLEM: ProblemDetail = {
  type: 'https://errors.asrevo.com/catalog/product-variant/sku-conflict',
  title: 'CATALOG.PRODUCT_VARIANT.SKU_CONFLICT',
  status: 409,
  detail: 'SKU ABC-1 already used by variant 42 in store 7',
  code: 'CATALOG.PRODUCT_VARIANT.SKU_CONFLICT',
  category: 'CONFLICT',
  params: {sku: 'ABC-1'},
  traceId: '3f9a1c8e',
};

describe('toApiError', () => {

  it('branch 1: returns an ApiError unchanged, so re-piping a stream cannot lose the body', () => {
    const original = toApiError(httpError({status: 409, error: PROBLEM}));

    expect(toApiError(original)).toBe(original);
  });

  it('branch 1: rehydrates a structurally-shaped error that lost its prototype', () => {
    const plain = JSON.parse(JSON.stringify({
      code: 'CATALOG.PRODUCT.NOT_FOUND', category: 'NOT_FOUND', status: 404, params: {}, fieldErrors: [],
    }));

    const error = toApiError(plain, URL);

    expect(error instanceof ApiError).toBeTrue();
    expect(error.code).toBe('CATALOG.PRODUCT.NOT_FOUND');
    expect(error.url).toBe(URL);
  });

  it('branch 2: status 0 is a network failure, not a server one', () => {
    const error = toApiError(httpError({status: 0, error: new ProgressEvent('error')}));

    expect(error.code).toBe('CLIENT.NETWORK_UNAVAILABLE');
    expect(error.category).toBe('NETWORK');
    expect(error.isServerSide).toBeTrue();
  });

  it('branch 3: reads a real problem body', () => {
    const error = toApiError(httpError({status: 409, error: PROBLEM}));

    expect(error.code).toBe('CATALOG.PRODUCT_VARIANT.SKU_CONFLICT');
    expect(error.category).toBe('CONFLICT');
    expect(error.status).toBe(409);
    expect(error.traceId).toBe('3f9a1c8e');
    expect(error.params).toEqual({sku: 'ABC-1'});
    expect(error.url).toBe(URL);
  });

  it('branch 3: keeps detail as debug-only and out of the message', () => {
    const error = toApiError(httpError({status: 409, error: PROBLEM}));

    expect(error.debugDetail).toBe('SKU ABC-1 already used by variant 42 in store 7');
    expect(error.message).toBe('CATALOG.PRODUCT_VARIANT.SKU_CONFLICT [409] traceId=3f9a1c8e');
    expect(error.message).not.toContain('ABC-1 already used');
  });

  it('branch 3: carries the remote-service pair when a peer failed', () => {
    const error = toApiError(httpError({
      status: 502,
      error: {code: 'COMMON.REMOTE_UNAVAILABLE', category: 'REMOTE_SERVICE', remoteService: 'payment', remoteStatus: 503},
    }));

    expect(error.remoteService).toBe('payment');
    expect(error.remoteStatus).toBe(503);
    expect(error.provider).toBeUndefined();
  });

  it('branch 3: carries the provider pair when a third party failed', () => {
    const error = toApiError(httpError({
      status: 422,
      error: {
        code: 'PAYMENT.INITIATE.REJECTED', category: 'UNPROCESSABLE',
        provider: 'stripe', providerCode: 'card_declined', providerStatus: 402,
      },
    }));

    expect(error.provider).toBe('stripe');
    expect(error.providerCode).toBe('card_declined');
    expect(error.remoteService).toBeUndefined();
    // A refusal is a decision, not an outage: it must not read as server-side.
    expect(error.isServerSide).toBeFalse();
  });

  it('branch 3: normalises field paths and drops malformed entries', () => {
    const error = toApiError(httpError({
      status: 400,
      error: {
        code: 'COMMON.VALIDATION_FAILED', category: 'VALIDATION',
        fieldErrors: [
          {field: 'items[0].sku', code: 'NotBlank', message: 'must not be blank'},
          {field: 'endpoint.endpoint', code: 'Pattern'},
          {code: 'NoField'},
        ],
      },
    }));

    expect(error.fieldErrors.length).toBe(2);
    expect(error.fieldErrors[0].field).toBe('items.0.sku');
    expect(error.fieldErrors[1].field).toBe('endpoint.endpoint');
    expect(error.isValidation).toBeTrue();
  });

  it('branch 3: does not trust a category it does not recognise', () => {
    const error = toApiError(httpError({status: 418, error: {code: 'X.Y', category: 'MADE_UP'}}));

    expect(error.code).toBe('CLIENT.HTTP_418');
  });

  it('branch 4: parses a body delivered as text', () => {
    const error = toApiError(httpError({status: 409, error: JSON.stringify(PROBLEM)}));

    expect(error.code).toBe('CATALOG.PRODUCT_VARIANT.SKU_CONFLICT');
    expect(error.status).toBe(409);
  });

  it('branch 4: falls through to the status when the text is not our contract', () => {
    const error = toApiError(httpError({status: 500, error: 'Internal Server Error'}));

    expect(error.code).toBe('CLIENT.HTTP_500');
    expect(error.category).toBe('INTERNAL');
  });

  it('branch 5: a ProgressEvent body is a network failure even with a status', () => {
    const error = toApiError(httpError({status: 504, error: new ProgressEvent('error')}));

    expect(error.category).toBe('NETWORK');
  });

  it('branch 6: synthesises from status for an HTML error page from the edge', () => {
    const error = toApiError(httpError({status: 502, error: '<html><body>502 Bad Gateway</body></html>'}));

    expect(error.code).toBe('CLIENT.HTTP_502');
    expect(error.category).toBe('REMOTE_SERVICE');
    expect(error.isServerSide).toBeTrue();
  });

  it('branch 6: synthesises for a null body', () => {
    const error = toApiError(httpError({status: 404, error: null}));

    expect(error.code).toBe('CLIENT.HTTP_404');
    expect(error.isNotFound).toBeTrue();
  });

  it('branch 7: anything that is not an HTTP failure', () => {
    const error = toApiError(new TypeError('cannot read properties of undefined'));

    expect(error.code).toBe('CLIENT.UNEXPECTED');
    expect(error.category).toBe('UNKNOWN');
    expect(error.status).toBe(0);
  });

  it('keeps 401 and 403 distinguishable — 403 must never trigger a login redirect', () => {
    const unauthenticated = toApiError(httpError({status: 401, error: {code: 'COMMON.UNAUTHENTICATED', category: 'UNAUTHENTICATED'}}));
    const forbidden = toApiError(httpError({status: 403, error: {code: 'COMMON.ACCESS_DENIED', category: 'FORBIDDEN'}}));

    expect(unauthenticated.isAuth).toBeTrue();
    expect(unauthenticated.isForbidden).toBeFalse();
    expect(forbidden.isForbidden).toBeTrue();
    expect(forbidden.isAuth).toBeFalse();
  });

});

describe('normalizeFieldPath', () => {

  it('converts array syntax to the dotted form AbstractControl.get understands', () => {
    expect(normalizeFieldPath('items[0].sku')).toBe('items.0.sku');
    expect(normalizeFieldPath('a[0].b[12].c')).toBe('a.0.b.12.c');
  });

  it('leaves a plain bean path alone', () => {
    expect(normalizeFieldPath('endpoint.endpoint')).toBe('endpoint.endpoint');
    expect(normalizeFieldPath('name')).toBe('name');
  });

  it('leaves the constraint-violation method prefix in place, for the form to resolve', () => {
    // No string rule can tell `createPod.pod.name` from a genuine `productType.code`; the form knows.
    expect(normalizeFieldPath('createPod.pod.name')).toBe('createPod.pod.name');
  });

});

describe('statusToCategory', () => {

  it('mirrors the backend enum for the statuses it fixes', () => {
    expect(statusToCategory(400)).toBe('VALIDATION');
    expect(statusToCategory(401)).toBe('UNAUTHENTICATED');
    expect(statusToCategory(403)).toBe('FORBIDDEN');
    expect(statusToCategory(404)).toBe('NOT_FOUND');
    expect(statusToCategory(409)).toBe('CONFLICT');
    expect(statusToCategory(413)).toBe('PAYLOAD_TOO_LARGE');
    expect(statusToCategory(422)).toBe('UNPROCESSABLE');
    expect(statusToCategory(502)).toBe('REMOTE_SERVICE');
    expect(statusToCategory(504)).toBe('TIMEOUT');
  });

  it('falls back sensibly for statuses with no category of their own', () => {
    expect(statusToCategory(451)).toBe('VALIDATION');
    expect(statusToCategory(507)).toBe('INTERNAL');
    expect(statusToCategory(302)).toBe('UNKNOWN');
  });

});
