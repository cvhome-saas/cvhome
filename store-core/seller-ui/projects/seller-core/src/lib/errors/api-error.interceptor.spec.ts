import {HttpClient, HttpContext, provideHttpClient, withInterceptors} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {ApiError} from './api-error';
import {apiErrorInterceptor, SKIP_ERROR_NORMALIZATION} from './api-error.interceptor';
import {SessionService} from './session.service';

class SpySessionService {
  calls = 0;

  onUnauthenticated(): void {
    this.calls++;
  }
}

const URL = '/api/v1/products';

function setup() {
  const session = new SpySessionService();
  TestBed.configureTestingModule({
    providers: [
      provideHttpClient(withInterceptors([apiErrorInterceptor])),
      provideHttpClientTesting(),
      {provide: SessionService, useValue: session},
    ],
  });
  return {
    http: TestBed.inject(HttpClient),
    httpMock: TestBed.inject(HttpTestingController),
    session,
  };
}

describe('apiErrorInterceptor', () => {

  afterEach(() => TestBed.inject(HttpTestingController).verify());

  it('delivers an ApiError to the caller, never an HttpErrorResponse', (done) => {
    const {http, httpMock} = setup();

    http.get(URL).subscribe({
      error: (error: unknown) => {
        expect(error instanceof ApiError).toBeTrue();
        expect((error as ApiError).code).toBe('CATALOG.PRODUCT.NOT_FOUND');
        expect((error as ApiError).category).toBe('NOT_FOUND');
        done();
      },
    });

    httpMock.expectOne(URL).flush(
      {code: 'CATALOG.PRODUCT.NOT_FOUND', category: 'NOT_FOUND', status: 404, traceId: 'abc123'},
      {status: 404, statusText: 'Not Found'});
  });

  it('records the request URL on the error, so a log line can be traced back', (done) => {
    const {http, httpMock} = setup();

    http.get(URL, {params: {store: 'org1-store1'}}).subscribe({
      error: (error: ApiError) => {
        expect(error.url).toBe(`${URL}?store=org1-store1`);
        done();
      },
    });

    httpMock.expectOne(r => r.url === URL).flush(null, {status: 500, statusText: 'Server Error'});
  });

  it('tells the session service about a 401', (done) => {
    const {http, httpMock, session} = setup();

    http.get(URL).subscribe({
      error: () => {
        expect(session.calls).toBe(1);
        done();
      },
    });

    httpMock.expectOne(URL).flush(
      {code: 'COMMON.UNAUTHENTICATED', category: 'UNAUTHENTICATED'},
      {status: 401, statusText: 'Unauthorized'});
  });

  it('does not treat a 403 as a session expiry', (done) => {
    // A seller lacking a permission is authenticated; redirecting them to login is a loop, not a fix.
    const {http, httpMock, session} = setup();

    http.get(URL).subscribe({
      error: (error: ApiError) => {
        expect(session.calls).toBe(0);
        expect(error.isForbidden).toBeTrue();
        done();
      },
    });

    httpMock.expectOne(URL).flush(
      {code: 'COMMON.ACCESS_DENIED', category: 'FORBIDDEN'},
      {status: 403, statusText: 'Forbidden'});
  });

  it('leaves a successful response untouched', (done) => {
    const {http, httpMock} = setup();

    http.get<{id: number}>(URL).subscribe(body => {
      expect(body).toEqual({id: 1});
      done();
    });

    httpMock.expectOne(URL).flush({id: 1});
  });

  it('skips normalisation when the caller opts out', (done) => {
    // dns-check reads a 404 as "this domain does not resolve" — data, not a fault.
    const {http, httpMock} = setup();
    const context = new HttpContext().set(SKIP_ERROR_NORMALIZATION, true);

    http.get(URL, {context}).subscribe({
      error: (error: unknown) => {
        expect(error instanceof ApiError).toBeFalse();
        done();
      },
    });

    httpMock.expectOne(URL).flush(null, {status: 404, statusText: 'Not Found'});
  });

  it('synthesises a typed error from a response with no problem body', (done) => {
    const {http, httpMock} = setup();

    http.get(URL).subscribe({
      error: (error: ApiError) => {
        // An HTML 502 from the pod edge still has to be describable.
        expect(error.code).toBe('CLIENT.HTTP_502');
        expect(error.category).toBe('REMOTE_SERVICE');
        done();
      },
    });

    httpMock.expectOne(URL).flush('<html>502 Bad Gateway</html>', {status: 502, statusText: 'Bad Gateway'});
  });

});
