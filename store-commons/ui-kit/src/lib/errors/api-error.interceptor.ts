import {HttpContextToken, HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {catchError, throwError} from 'rxjs';
import {toApiError} from './problem-detail.parser';
import {SessionService} from './session.service';

/**
 * Opt a single request out of normalisation, for the few callers that treat a failure status as a
 * legitimate answer rather than an error.
 *
 * ```ts
 * this.http.get(url, {context: new HttpContext().set(SKIP_ERROR_NORMALIZATION, true)})
 * ```
 *
 * `dns-check` is the real case: a 404 there means "this domain does not resolve", which is data, not a
 * fault.
 */
export const SKIP_ERROR_NORMALIZATION = new HttpContextToken<boolean>(() => false);

/**
 * Turns every HTTP failure into an `ApiError` before it reaches a caller.
 *
 * This belongs in an interceptor rather than in `CrudService`, for three reasons that only became clear
 * from the code:
 *
 * - `CrudService` is not the only HTTP client. Direct `HttpClient` callers would silently keep the old
 *   behaviour without an interceptor.
 * - `CrudService.request()` returns `Observable<HttpEvent<unknown>>` for upload progress, which a blanket
 *   `catchError` complicates.
 * - Only an interceptor sees the `HttpRequest`, which is what makes the opt-out above expressible at all.
 *
 * **It deliberately does not toast.** Whether a failure becomes a toast, an inline message or a set of
 * field errors is a call-site decision; only the expired session is global enough to handle here.
 */
export const apiErrorInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.context.get(SKIP_ERROR_NORMALIZATION)) {
    return next(req);
  }

  const session = inject(SessionService);

  return next(req).pipe(
    catchError((raw: unknown) => {
      const error = toApiError(raw, req.urlWithParams);
      if (error.isAuth) {
        session.onUnauthenticated(error);
      }
      return throwError(() => error);
    }),
  );
};
