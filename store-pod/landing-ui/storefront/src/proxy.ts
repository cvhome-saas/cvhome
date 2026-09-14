import createMiddleware from 'next-intl/middleware';
import {NextRequest, NextResponse} from 'next/server';
import {routing} from '@store-front/i18n/routing';
import {FALLBACK_STORE_ID} from '@store-front/types/constant';
import {COLOR_OVERRIDE_COOKIE, THEME_OVERRIDE_COOKIE, themeOverrideEnabled} from '@/shell/theme/override';
import {resolveThemeId, type RegisteredThemeId} from '@/shell/theme/theme-id';

/**
 * Edge logic that used to live in the Express server (`templates-deprecated/express-app`):
 *  1. No `Store-Id` header (injected by spg/Caddy `domain_lookup`) → the store-not-found page, 404.
 *  2. `/` → `/{lang}` using the NEXT_LOCALE cookie, then the `Default-Language` header, constrained by
 *     `Supported-Languages`.
 *  3. Everything else → next-intl locale routing.
 *  4. The store's theme picks its route tree: `/{locale}/…` is rewritten (never redirected, so the browser keeps
 *     its URL and client navigations take the same path) to `/t/{theme}/{locale}/…`, a tree that imports that
 *     theme alone — which is what keeps every other theme's CSS and JS off the page. `/t/…` from outside is a 404.
 *  5. Dev/QA only: `?theme=<id>` and `?color=<ColorTheme|default>` persist override cookies, read here (the tree)
 *     and by `getColorThemeRequest()`; an empty value clears the cookie.
 */
const intlMiddleware = createMiddleware(routing);

const OVERRIDE_PARAMS: readonly (readonly [param: string, cookie: string])[] = [['theme', THEME_OVERRIDE_COOKIE], ['color', COLOR_OVERRIDE_COOKIE]];

const TREE_PREFIX = '/t';

/**
 * The theme this request renders: the `?theme=` of this very request when overrides are on (its cookie is only set
 * on the response), then the override cookie, the `Theme` header spg adds, `STOREFRONT_THEME`, the fallback — the
 * order `getThemeId()` keeps for `/api/theme-manifest`.
 */
function requestThemeId(req: NextRequest): RegisteredThemeId {
    let override: string | undefined;
    if (themeOverrideEnabled()) {
        override = req.nextUrl.searchParams.has('theme')
            ? req.nextUrl.searchParams.get('theme') ?? ''
            : req.cookies.get(THEME_OVERRIDE_COOKIE)?.value;
    }
    return resolveThemeId(override || req.headers.get('theme') || process.env.STOREFRONT_THEME);
}

/**
 * next-intl's answer, sent into the theme's tree instead. Its request-header overrides (the resolved locale), its
 * cookies and its headers ride along; if it rewrote to an internal path itself, that path is the one re-targeted.
 */
function intoThemeTree(req: NextRequest, intl: NextResponse, theme: RegisteredThemeId): NextResponse {
    const internal = intl.headers.get('x-middleware-rewrite');
    const target = internal ? new URL(internal) : req.nextUrl;
    const res = NextResponse.rewrite(new URL(`${TREE_PREFIX}/${theme}${target.pathname}${target.search}`, req.url));
    intl.headers.forEach((value, key) => {
        if (key === 'x-middleware-next' || key === 'x-middleware-rewrite' || key === 'set-cookie') return;
        res.headers.set(key, value);
    });
    for (const cookie of intl.cookies.getAll()) res.cookies.set(cookie);
    return res;
}

export default function proxy(req: NextRequest) {
    const h = req.headers;
    const requested = req.nextUrl.pathname;
    if (requested === TREE_PREFIX || requested.startsWith(`${TREE_PREFIX}/`)) {
        return new NextResponse(null, {status: 404});
    }
    // Through spg the header is always present; hitting :8110 directly (local dev) falls back to the env var,
    // then to the demo store constant — same chain as extractSsrContext().
    const storeId = h.get('store-id') ?? process.env.FALLBACK_STORE_ID ?? FALLBACK_STORE_ID;
    if (!storeId) {
        return NextResponse.rewrite(new URL('/store-not-found', req.url), {status: 404});
    }

    const {pathname, searchParams} = req.nextUrl;
    let res: NextResponse;

    if (pathname === '/' || pathname === '') {
        const supported = (h.get('supported-languages') ?? '').split(',').map(s => s.trim()).filter(Boolean);
        const cookie = req.cookies.get('NEXT_LOCALE')?.value;
        const want = [cookie, h.get('default-language'), routing.defaultLocale]
            .find((l): l is string => !!l && (routing.locales as readonly string[]).includes(l) && (supported.length === 0 || supported.includes(l)))
            ?? routing.defaultLocale;
        const target = new URL(`/${want}`, req.url);
        target.search = req.nextUrl.search;
        res = NextResponse.redirect(target);
    } else {
        res = intlMiddleware(req);
        const isRedirect = res.status >= 300 && res.status < 400 && res.headers.has('location');
        if (!isRedirect) res = intoThemeTree(req, res, requestThemeId(req));
    }

    if (themeOverrideEnabled()) {
        for (const [param, cookie] of OVERRIDE_PARAMS) {
            if (!searchParams.has(param)) continue;
            const value = searchParams.get(param) ?? '';
            if (value) res.cookies.set(cookie, value, {path: '/', sameSite: 'lax'});
            else res.cookies.delete(cookie);
        }
    }
    return res;
}

export const config = {
    matcher: [
        // Skip API-ish paths, Next internals, the system 404 page and anything with a file extension.
        '/((?!api|trpc|_next|_vercel|store-not-found|.*\\..*).*)',
        // Except under /t: a tree's payload answers at `<path>.rsc` too, and the extension rule above would let that
        // form reach the tree without the 404 below.
        '/t/:path*',
    ],
};
