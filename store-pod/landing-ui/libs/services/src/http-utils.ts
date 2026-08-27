import {
    ApiError,
    CLIENT_ERROR_CODES,
    ErrorCategory,
    ProblemDetail,
    ProblemFieldError,
    ServerErrorCategory,
} from "@store-front/types/api-error";
import {AuthEventType} from "@store-front/types/auth";

/**
 * Mirrors the status each `ErrorCategory` fixes, for the responses that carry no problem body at all —
 * a Caddy HTML 502, a gateway timeout, an empty 500. Without this a shopper sees "something went wrong"
 * for a case we can describe precisely.
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

const SERVER_CATEGORIES: ReadonlySet<string> = new Set<ServerErrorCategory>([
    'VALIDATION', 'MALFORMED', 'CONVERSION', 'UNAUTHENTICATED', 'FORBIDDEN', 'NOT_FOUND', 'CONFLICT',
    'PAYLOAD_TOO_LARGE', 'UNPROCESSABLE', 'STORAGE', 'INTERNAL', 'REMOTE_SERVICE', 'TIMEOUT',
]);

function categoryForStatus(status: number): ErrorCategory {
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
 * A body counts as the real contract only when `code` and `category` are both strings. Anything else — an
 * HTML error page from the edge, a bare string, an empty body — is synthesised from the status instead.
 */
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
    return body.fieldErrors.filter((it): it is ProblemFieldError =>
        typeof it === 'object' && it !== null && typeof it.field === 'string');
}

/**
 * Turns a failed {@link Response} into a typed {@link ApiError}. Reads the body as text first so a
 * non-JSON error page cannot throw while we are already handling a failure.
 */
export async function toApiError(res: Response, url?: string): Promise<ApiError> {
    let body: unknown;
    try {
        const text = await res.text();
        body = text ? JSON.parse(text) : undefined;
    } catch {
        body = undefined;
    }

    const target = url ?? res.url;

    if (isProblemDetail(body)) {
        return new ApiError({
            code: body.code as string,
            category: body.category as ErrorCategory,
            status: typeof body.status === 'number' ? body.status : res.status,
            traceId: body.traceId,
            params: body.params,
            fieldErrors: readFieldErrors(body),
            url: target,
            remoteService: body.remoteService,
            remoteStatus: body.remoteStatus,
            provider: body.provider,
            providerCode: body.providerCode,
            providerStatus: body.providerStatus,
            // Developer text: kept for the log, never rendered.
            debugDetail: body.detail,
        });
    }

    return new ApiError({
        code: `CLIENT.HTTP_${res.status}`,
        category: categoryForStatus(res.status),
        status: res.status,
        url: target,
    });
}

/** A failure that never reached a server — DNS, CORS, offline, an aborted connection. */
export function toNetworkError(cause: unknown, url?: string): ApiError {
    return new ApiError({
        code: CLIENT_ERROR_CODES.NETWORK_UNAVAILABLE,
        category: 'NETWORK',
        status: 0,
        url,
        cause,
    });
}

/**
 * Throws an {@link ApiError} on any non-2xx.
 *
 * It used to log the status and return `undefined`, which meant the status and body were both gone and —
 * because it resolved rather than rejected — every `.catch(...)` downstream was dead code for backend
 * failures. That is why the checkout error branch had never run in production.
 */
export async function handleResponse<T>(res: Response, url?: string): Promise<T> {
    if (!res.ok) {
        throw await toApiError(res, url);
    }
    if (res.status === 204) {
        return undefined as T;
    }
    return await res.json() as T;
}

/**
 * `fetch` + {@link handleResponse}, with network failures typed as well. Prefer this over calling `fetch`
 * directly: a bare `fetch` rejects with a `TypeError` that no caller can branch on.
 */
export async function apiFetch<T>(url: string, init?: RequestInit): Promise<T> {
    let response: Response;
    try {
        response = await fetch(url, init);
    } catch (cause) {
        throw toNetworkError(cause, url);
    }
    if (response.status === 401 && sentCredential(init)) {
        discardRejectedToken();
    }
    return handleResponse<T>(response, url);
}

function sentCredential(init?: RequestInit): boolean {
    const headers = init?.headers as Record<string, string> | undefined;
    return Boolean(headers && headers['Authorization']);
}

/**
 * Drop a token the server has just refused, and tell the app the shopper is signed out.
 *
 * A rejected token is not a transient failure — it is expired, or it was minted by an issuer this pod no
 * longer trusts. Left in `sessionStorage` it is presented again on every subsequent call and every reload, so
 * one bad token used to wedge the session until someone cleared storage by hand. `useUser` already listens for
 * `auth-change`, so clearing here is enough for the UI to fall back to signed-out.
 */
function discardRejectedToken(): void {
    if (typeof window === 'undefined') {
        return;
    }
    if (!sessionStorage.getItem('access_token')) {
        return;
    }
    console.warn('Access token rejected; discarding it and continuing signed out.');
    sessionStorage.removeItem('access_token');
    sessionStorage.removeItem('id_token');
    sessionStorage.removeItem('refresh_token');
    window.dispatchEvent(new CustomEvent('auth-change', {detail: {type: AuthEventType.LOGOUT}}));
}

/**
 * Opt out of failing, explicitly.
 *
 * Swallowing used to be the invisible default for all 23 call sites. Where a degraded rendering really is
 * right — a content box that does not load, a recommendations strip — wrap the call in this so the choice
 * is greppable and deliberate rather than the behaviour of the transport.
 */
export async function orUndefined<T>(promise: Promise<T>): Promise<T | undefined> {
    try {
        return await promise;
    } catch (error) {
        console.warn('Optional request failed, rendering without it:',
            error instanceof ApiError ? error.toLogContext() : error);
        return undefined;
    }
}

/**
 * Whether a call carries the shopper's bearer token.
 *
 * `auth: true` means "send it if we have one" — which covers both the endpoints that require identity
 * (`/private/customer/**`, cua's `/auth/me`) and the ones that merely use it when present (checkout, order
 * status, which link the order to a signed-in shopper and enforce a store's login requirement).
 */
export interface RequestOptions {
    auth?: boolean;
}

function buildHeader<T>(method: string, it?: T, options?: RequestOptions): RequestInit {
    const accessToken = options?.auth && typeof window !== 'undefined'
        ? sessionStorage.getItem('access_token')
        : undefined;
    const headers: Record<string, string> = {};
    if (it) {
        headers["Content-Type"] = "application/json";
    }
    if (accessToken) {
        headers["Authorization"] = `Bearer ${accessToken}`;
    }
    const result: RequestInit = {
        method: method,
        headers
    };
    if (it) {
        result.body = JSON.stringify(it);
    }
    return result;
}

export function post<T>(it: T, options?: RequestOptions) {
    return buildHeader('POST', it, options);
}

export function put<T>(it: T, options?: RequestOptions) {
    return buildHeader('PUT', it, options);
}

export function del(options?: RequestOptions) {
    return buildHeader('DELETE', undefined, options);
}

/**
 * Anonymous by default. Every call used to carry whatever was in `sessionStorage`, which meant a stale token
 * was presented to the catalog and content APIs that have no interest in identity — and, while those APIs
 * answered a rejected token with a 500, wedged the whole storefront for a signed-in shopper. Public reads have
 * no business carrying a credential; pass `{auth: true}` where identity is actually part of the request.
 */
export function get(options?: RequestOptions) {
    return buildHeader('GET', undefined, options);
}
