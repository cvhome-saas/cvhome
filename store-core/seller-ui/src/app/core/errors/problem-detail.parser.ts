import {HttpErrorResponse} from '@angular/common/http';
import {ApiError, isApiError} from './api-error';
import {
  CLIENT_ERROR_CODES,
  ErrorCategory,
  ProblemDetail,
  ProblemFieldError,
  SERVER_CATEGORIES,
  ServerErrorCategory,
} from './problem-detail.model';

/**
 * Mirrors the status each `ErrorCategory` fixes, for responses carrying no problem body at all — a Caddy
 * HTML 502, an empty 500, a blob body. Without it those all collapse into "something went wrong" when we
 * can in fact describe them.
 */
const STATUS_TO_CATEGORY: Readonly<Record<number, ServerErrorCategory>> = {
  400: 'VALIDATION',
  401: 'UNAUTHENTICATED',
  403: 'FORBIDDEN',
  404: 'NOT_FOUND',
  409: 'CONFLICT',
  413: 'PAYLOAD_TOO_LARGE',
  422: 'UNPROCESSABLE',
  500: 'INTERNAL',
  502: 'REMOTE_SERVICE',
  503: 'REMOTE_SERVICE',
  504: 'TIMEOUT',
};

export function statusToCategory(status: number): ErrorCategory {
  const mapped = STATUS_TO_CATEGORY[status];
  if (mapped) {
    return mapped;
  }
  if (status >= 500) {
    return 'INTERNAL';
  }
  return status >= 400 ? 'VALIDATION' : 'UNKNOWN';
}

/**
 * Converts a bean path's array syntax to the dotted form `AbstractControl.get()` understands:
 * `items[0].sku` → `items.0.sku`.
 *
 * It deliberately does **not** try to strip the method segment that `ConstraintViolationErrorHandler`
 * prefixes (`createPod.pod.name` → `pod.name`). No rule can tell that apart from a genuine two-level bean
 * path by looking at the string — `productType.code` is real. `applyFieldErrors` resolves it instead by
 * asking the form: it looks up the full path, and only on a miss retries without the leading segment. The
 * form is the one oracle that actually knows.
 */
export function normalizeFieldPath(field: string): string {
  return field.replace(/\[(\d+)\]/g, '.$1');
}

/** A body is the real contract only when `code` and `category` are both strings we recognise. */
function isProblemDetail(body: unknown): body is ProblemDetail {
  if (typeof body !== 'object' || body === null) {
    return false;
  }
  const candidate = body as ProblemDetail;
  return typeof candidate.code === 'string' && typeof candidate.category === 'string'
    && SERVER_CATEGORIES.has(candidate.category);
}

function readFieldErrors(body: ProblemDetail): readonly ProblemFieldError[] {
  if (!Array.isArray(body.fieldErrors)) {
    return [];
  }
  return body.fieldErrors
    .filter((it): it is ProblemFieldError => typeof it === 'object' && it !== null && typeof it.field === 'string')
    .map(it => ({...it, field: normalizeFieldPath(it.field)}));
}

function fromProblemDetail(body: ProblemDetail, fallbackStatus: number, url?: string): ApiError {
  return new ApiError({
    code: body.code as string,
    category: body.category as ErrorCategory,
    status: typeof body.status === 'number' ? body.status : fallbackStatus,
    traceId: body.traceId,
    params: body.params,
    fieldErrors: readFieldErrors(body),
    url,
    remoteService: body.remoteService,
    remoteStatus: body.remoteStatus,
    provider: body.provider,
    providerCode: body.providerCode,
    providerStatus: body.providerStatus,
    // Developer text: kept for the log, dropped from anything user-facing.
    debugDetail: body.detail,
  });
}

/**
 * Normalises whatever the HTTP layer threw into an `ApiError`. Pure, so it can be unit-tested directly and
 * has no DI of its own.
 *
 * The branches are ordered: the cheap identity check first, then the cases that carry real information,
 * and only then the synthesised fallbacks.
 */
export function toApiError(raw: unknown, url?: string): ApiError {
  // 1. Idempotent — `CrudService.request()` streams get re-piped, and re-wrapping would lose the body.
  if (raw instanceof ApiError) {
    return raw;
  }
  if (isApiError(raw)) {
    const it = raw as ApiError;
    return new ApiError({...it, url: it.url ?? url});
  }

  if (raw instanceof HttpErrorResponse) {
    const target = url ?? raw.url ?? undefined;

    // 2. The request never arrived: no status, so nothing server-side to read.
    if (raw.status === 0) {
      return networkError(target, raw);
    }

    const body: unknown = raw.error;

    // 3. The real thing.
    if (isProblemDetail(body)) {
      return fromProblemDetail(body, raw.status, target);
    }

    // 4. A body delivered as text — `responseType: 'text'`, or a proxy that dropped the content type.
    if (typeof body === 'string') {
      const parsed = tryParse(body);
      if (isProblemDetail(parsed)) {
        return fromProblemDetail(parsed, raw.status, target);
      }
    }

    // 5. A browser-level failure surfaced as an event rather than a response.
    if (typeof ProgressEvent !== 'undefined' && body instanceof ProgressEvent) {
      return networkError(target, raw);
    }

    // 6. A response with no usable body: an HTML 502 from the edge, a blob, an empty 500.
    return new ApiError({
      code: `CLIENT.HTTP_${raw.status}`,
      category: statusToCategory(raw.status),
      status: raw.status,
      url: target,
      cause: raw,
    });
  }

  // 7. Something that is not an HTTP failure at all — a bug in a pipe, a thrown string.
  return new ApiError({
    code: CLIENT_ERROR_CODES.UNEXPECTED,
    category: 'UNKNOWN',
    status: 0,
    url,
    cause: raw,
  });
}

function networkError(url: string | undefined, cause: unknown): ApiError {
  return new ApiError({
    code: CLIENT_ERROR_CODES.NETWORK_UNAVAILABLE,
    category: 'NETWORK',
    status: 0,
    url,
    cause,
  });
}

function tryParse(text: string): unknown {
  try {
    return JSON.parse(text);
  } catch {
    return undefined;
  }
}
