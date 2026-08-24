import createMiddleware from 'next-intl/middleware';
import {NextRequest, NextResponse} from 'next/server';
import {routing} from '@store-front/i18n/routing';
import {FALLBACK_STORE_ID} from '@store-front/types/constant';
import {COLOR_OVERRIDE_COOKIE, THEME_OVERRIDE_COOKIE} from '@/shell/theme/override';

/**
 * Edge logic that used to live in the Express server (`templates-deprecated/express-app`):
 *  1. No `Store-Id` header (injected by spg/Caddy `domain_lookup`) → the store-not-found page, 404.
 *  2. `/` → `/{lang}` using the NEXT_LOCALE cookie, then the `Default-Language` header, constrained by
 *     `Supported-Languages`.
 *  3. Everything else → next-intl locale routing.
 *  4. Dev/QA only: `?theme=<id>` / `?color=<preset>` persist override cookies read by
 *     `getTheme()` / `resolveMerchantTokens()`.
 */
const intlMiddleware = createMiddleware(routing);


const overrideEnabled = process.env.NODE_ENV !== 'production' || process.env.STOREFRONT_THEME_OVERRIDE === 'true';

export default function proxy(req: NextRequest) {
    const h = req.headers;
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
    }

    if (overrideEnabled) {
        for (const [param, cookie] of [['theme', THEME_OVERRIDE_COOKIE], ['color', COLOR_OVERRIDE_COOKIE]] as const) {
            if (!searchParams.has(param)) continue;
            const value = searchParams.get(param) ?? '';
            if (value) res.cookies.set(cookie, value, {path: '/', sameSite: 'lax'});
            else res.cookies.delete(cookie);
        }
    }
    return res;
}

export const config = {
    // Skip API-ish paths, Next internals, the system 404 page and anything with a file extension.
    matcher: '/((?!api|trpc|_next|_vercel|store-not-found|.*\\..*).*)',
};
