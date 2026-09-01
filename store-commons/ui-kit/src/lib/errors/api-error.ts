import {ErrorCategory, ErrorParams, ProblemFieldError} from './problem-detail.model';

/**
 * A backend failure, typed.
 *
 * Every observable that leaves the HTTP layer errors with one of these rather than an `HttpErrorResponse`,
 * so a call site can branch on `code`, bind `fieldErrors` to controls, and quote a `traceId` to support.
 *
 * `message` is deliberately a diagnostic string and never user copy: a stray `{{err.message}}` left in a
 * template then leaks something that plainly is not a translation, rather than something that looks like
 * one. User-facing text comes from `ApiErrorService`'s code → category → generic chain.
 */
export class ApiError extends Error {

  readonly code: string;

  readonly category: ErrorCategory;

  readonly status: number;

  readonly traceId?: string;

  readonly params: ErrorParams;

  readonly fieldErrors: readonly ProblemFieldError[];

  readonly url?: string;

  readonly remoteService?: string;

  readonly remoteStatus?: number;

  readonly provider?: string;

  readonly providerCode?: string;

  readonly providerStatus?: number;

  /**
   * The server's developer text. Log it, never render it. `includeDebugDetail` is false in production so
   * it is usually absent, and when present it may carry internal specifics.
   */
  readonly debugDetail?: string;

  constructor(init: {
    code: string;
    category: ErrorCategory;
    status: number;
    traceId?: string;
    params?: ErrorParams;
    fieldErrors?: readonly ProblemFieldError[];
    url?: string;
    remoteService?: string;
    remoteStatus?: number;
    provider?: string;
    providerCode?: string;
    providerStatus?: number;
    debugDetail?: string;
    cause?: unknown;
  }) {
    super(`${init.code} [${init.status}]${init.traceId ? ` traceId=${init.traceId}` : ''}`);
    this.name = 'ApiError';
    this.code = init.code;
    this.category = init.category;
    this.status = init.status;
    this.traceId = init.traceId;
    this.params = init.params ?? {};
    this.fieldErrors = init.fieldErrors ?? [];
    this.url = init.url;
    this.remoteService = init.remoteService;
    this.remoteStatus = init.remoteStatus;
    this.provider = init.provider;
    this.providerCode = init.providerCode;
    this.providerStatus = init.providerStatus;
    this.debugDetail = init.debugDetail;
    if (init.cause !== undefined) {
      this.cause = init.cause;
    }
  }

  get isValidation(): boolean {
    return this.category === 'VALIDATION' || this.category === 'MALFORMED' || this.category === 'CONVERSION';
  }

  get isAuth(): boolean {
    return this.category === 'UNAUTHENTICATED';
  }

  /**
   * Kept apart from `isAuth` on purpose. A seller who lacks a permission is authenticated — bouncing them
   * to a login they are already past is a loop, not a fix.
   */
  get isForbidden(): boolean {
    return this.category === 'FORBIDDEN';
  }

  get isNotFound(): boolean {
    return this.category === 'NOT_FOUND';
  }

  /** Ours or a peer's fault, not the seller's — the cases where a trace reference is worth showing. */
  get isServerSide(): boolean {
    return this.status >= 500 || this.category === 'NETWORK';
  }

  /** The subset worth logging. The only place `debugDetail` may travel. */
  toLogContext(): Record<string, unknown> {
    return {
      code: this.code,
      category: this.category,
      status: this.status,
      traceId: this.traceId,
      url: this.url,
      remoteService: this.remoteService,
      provider: this.provider,
      providerCode: this.providerCode,
      detail: this.debugDetail,
    };
  }
}

/**
 * Structural rather than `instanceof`. Angular SSR runs the app in two contexts, and an error that has been
 * serialised between them has lost its prototype; the shape is what survives.
 */
export function isApiError(value: unknown): value is ApiError {
  if (value instanceof ApiError) {
    return true;
  }
  if (typeof value !== 'object' || value === null) {
    return false;
  }
  const candidate = value as Partial<ApiError>;
  return typeof candidate.code === 'string' && typeof candidate.category === 'string'
    && typeof candidate.status === 'number';
}
