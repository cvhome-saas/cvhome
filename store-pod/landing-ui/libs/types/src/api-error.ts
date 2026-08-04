/**
 * The frontend mirror of the backend's error contract.
 *
 * Every one of the 8 cvhome services emits the same extended RFC-7807 body, built only by
 * `ProblemDetailFactory`. Before this module the storefront threw all of it away: `handleResponse`
 * discarded the status and the body on any non-2xx and returned `undefined`, so a 422 "card declined"
 * and a 502 "payment service unreachable" were the same nothing.
 */

/**
 * The categories the backend can send, mirroring `store-commons/errors/.../ErrorCategory.java`.
 *
 * The category is the guaranteed fallback: every code has one, so a message can always be resolved even
 * for the ~125 un-migrated `LEGACY.*` codes that have no translation of their own.
 */
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
 * Categories the client synthesises when there is no server answer to read. They are not in the backend
 * enum, which is exactly why they are named apart: `NETWORK` means the request never arrived.
 */
export type ClientErrorCategory = 'NETWORK' | 'UNKNOWN';

export type ErrorCategory = ServerErrorCategory | ClientErrorCategory;

/** A flat map of context values, interpolated straight into the translated message. */
export type ErrorParams = Readonly<Record<string, unknown>>;

/**
 * One field-level failure. `field` arrives in two shapes — bean paths (`items[0].sku`) from
 * `GlobalErrorHandler` and method-prefixed constraint paths (`createPod.pod.name`) from
 * `ConstraintViolationErrorHandler` — so it is normalised before it reaches a form control.
 */
export interface ProblemFieldError {
    readonly field: string;
    readonly code: string;
    readonly message?: string;
    readonly params?: ErrorParams;
}

/** The wire body, exactly as `ProblemDetailFactory` writes it. */
export interface ProblemDetail {
    readonly type?: string;
    readonly title?: string;
    readonly status?: number;
    /** Developer text. Never render it — see {@link ApiError.debugDetail}. */
    readonly detail?: string;
    readonly code?: string;
    readonly category?: string;
    readonly params?: ErrorParams;
    readonly fieldErrors?: readonly ProblemFieldError[];
    readonly traceId?: string;
    /** Set when a peer cvhome service failed. Mutually exclusive with {@link provider}. */
    readonly remoteService?: string;
    readonly remoteStatus?: number;
    /** Set when a third party (Stripe) failed. Mutually exclusive with {@link remoteService}. */
    readonly provider?: string;
    readonly providerCode?: string;
    readonly providerStatus?: number;
}

/** Codes this client raises itself, for the failures that never reach a server. */
export const CLIENT_ERROR_CODES = {
    NETWORK_UNAVAILABLE: 'CLIENT.NETWORK_UNAVAILABLE',
    UNEXPECTED: 'CLIENT.UNEXPECTED',
    CART_EMPTY: 'CLIENT.CART.EMPTY',
} as const;

/**
 * A backend failure, typed.
 *
 * `message` is deliberately a diagnostic string and never user copy, so a stray `{error.message}` in a
 * template leaks something that plainly is not a translation rather than something that looks like one.
 * User-facing text comes from the code/category translation chain.
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
     * The server's developer text. Log it, never render it. `includeDebugDetail` is false in production
     * so it is usually absent, and when present it may carry internal specifics.
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
        super(`${init.code} [${init.status}]${init.traceId ? ` traceId=${init.traceId}` : ''}`,
            init.cause === undefined ? undefined : {cause: init.cause});
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
    }

    get isValidation(): boolean {
        return this.category === 'VALIDATION' || this.category === 'MALFORMED' || this.category === 'CONVERSION';
    }

    get isAuth(): boolean {
        return this.category === 'UNAUTHENTICATED';
    }

    get isForbidden(): boolean {
        return this.category === 'FORBIDDEN';
    }

    get isNotFound(): boolean {
        return this.category === 'NOT_FOUND';
    }

    /** Ours or a peer's fault, not the shopper's — the cases where a trace reference is worth showing. */
    get isServerSide(): boolean {
        return this.status >= 500 || this.category === 'NETWORK';
    }

    /**
     * Nothing was decided: the request may or may not have taken effect.
     *
     * This is the distinction the whole checkout flow turns on. `PAYMENT.INITIATE.REJECTED` (422) is a
     * decision — the card was refused, retrying will not help. `PAYMENT.INITIATE.FAILED` (502) is no
     * answer at all — the payment may have started, so telling the shopper it failed is how an order gets
     * cancelled after being charged.
     */
    get isUndecided(): boolean {
        return this.category === 'REMOTE_SERVICE' || this.category === 'TIMEOUT' || this.category === 'NETWORK';
    }

    /** The subset worth logging. Never includes anything user-facing. */
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
 * Structural rather than `instanceof`: an error crossing the Next.js server/client boundary is serialised
 * and arrives as a plain object, so the prototype is gone by the time a boundary sees it.
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
