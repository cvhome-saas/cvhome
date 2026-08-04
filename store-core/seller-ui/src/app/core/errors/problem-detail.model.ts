/**
 * An exact mirror of the wire types every cvhome service emits, built only by `ProblemDetailFactory`.
 *
 * This lives in `core/` rather than `pages/shared/` because it has to serve both `pages/**` and
 * `public/**`, and `public` cannot import from `pages/shared` without inverting the dependency.
 */

/** Mirrors `store-commons/errors/.../ErrorCategory.java`. The number is the status it fixes. */
export type ServerErrorCategory =
  | 'VALIDATION'          // 400
  | 'MALFORMED'           // 400
  | 'CONVERSION'          // 400
  | 'UNAUTHENTICATED'     // 401
  | 'FORBIDDEN'           // 403
  | 'NOT_FOUND'           // 404
  | 'CONFLICT'            // 409
  | 'PAYLOAD_TOO_LARGE'   // 413
  | 'UNPROCESSABLE'       // 422
  | 'STORAGE'             // 500
  | 'INTERNAL'            // 500
  | 'REMOTE_SERVICE'      // 502
  | 'TIMEOUT';            // 504

/**
 * Categories the client synthesises when there is no server answer to read. Deliberately named apart from
 * the server set: `NETWORK` means the request never arrived, which no backend can tell us.
 */
export type ClientErrorCategory = 'NETWORK' | 'UNKNOWN';

export type ErrorCategory = ServerErrorCategory | ClientErrorCategory;

/** A flat map of context values, interpolated into the translated message by ngx-translate. */
export type ErrorParams = Readonly<Record<string, unknown>>;

/**
 * One field-level failure.
 *
 * `field` arrives in two shapes: bean paths (`endpoint.endpoint`, `items[0].sku`) from `GlobalErrorHandler`,
 * and `jakarta.validation` property paths prefixed with the method name (`createPod.pod.name`) from
 * `ConstraintViolationErrorHandler`.
 */
export interface ProblemFieldError {
  readonly field: string;
  readonly code: string;
  readonly message?: string;
  readonly params?: ErrorParams;
}

/** The response body itself. */
export interface ProblemDetail {
  readonly type?: string;
  readonly title?: string;
  readonly status?: number;
  /** Developer text. Never render it — see `ApiError.debugDetail`. */
  readonly detail?: string;
  readonly code?: string;
  readonly category?: string;
  readonly params?: ErrorParams;
  readonly fieldErrors?: readonly ProblemFieldError[];
  readonly traceId?: string;
  /** Set when a peer cvhome service failed. Mutually exclusive with `provider`. */
  readonly remoteService?: string;
  readonly remoteStatus?: number;
  /** Set when a third party (Stripe) failed. Mutually exclusive with `remoteService`. */
  readonly provider?: string;
  readonly providerCode?: string;
  readonly providerStatus?: number;
}

/** The server categories, as a runtime set — a `category` we do not recognise is not trusted. */
export const SERVER_CATEGORIES: ReadonlySet<string> = new Set<ServerErrorCategory>([
  'VALIDATION', 'MALFORMED', 'CONVERSION', 'UNAUTHENTICATED', 'FORBIDDEN', 'NOT_FOUND', 'CONFLICT',
  'PAYLOAD_TOO_LARGE', 'UNPROCESSABLE', 'STORAGE', 'INTERNAL', 'REMOTE_SERVICE', 'TIMEOUT',
]);

/** Codes this client raises itself, for failures that never reach a server. */
export const CLIENT_ERROR_CODES = {
  NETWORK_UNAVAILABLE: 'CLIENT.NETWORK_UNAVAILABLE',
  UNEXPECTED: 'CLIENT.UNEXPECTED',
} as const;
