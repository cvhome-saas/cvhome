import {FALLBACK_LOCALE, STORE_DEFAULT_LOCALE_COOKIE_NAME} from "@/types/constant";
import {Store} from "@/types/store";
import type {ReadonlyHeaders} from "next/dist/server/web/spec-extension/adapters/headers";

export function getLocaleFromCookie(): string | null {
    if (typeof document !== 'undefined') {
        const match = document.cookie.match(new RegExp(`(^| )${STORE_DEFAULT_LOCALE_COOKIE_NAME}=([^;]+)`));
        return match ? decodeURIComponent(match[2]) : null;
    }
    return null;
}

export function setLocaleCookie(): string {
    if (typeof document !== 'undefined') {
        document.cookie = `${STORE_DEFAULT_LOCALE_COOKIE_NAME}=${encodeURIComponent(FALLBACK_LOCALE)}; path=/`;
    }
    return FALLBACK_LOCALE;
}

export function buildDefaultLangRedirectionUrl(store: Store, headers: ReadonlyHeaders, urlLocale: string): string {
    // Construct the new path for redirection
    let newPath: string;
    // Try to get the full path from headers if no slug (e.g. for base path /en)
    // x-middleware-request-next-url is often more reliable if middleware is involved
    const requestUrlHeader = headers.get('x-middleware-request-next-url') || headers.get('x-next-pathname');

    if (requestUrlHeader) {
        try {
            const currentUrl = new URL(requestUrlHeader);
            const pathSegments = currentUrl.pathname.split('/').filter(Boolean); // e.g. ['en', 'some', 'page'] or ['en']
            if (pathSegments.length > 0 && pathSegments[0] === urlLocale) {
                pathSegments[0] = store.defaultLanguage;
                newPath = `/${pathSegments.join('/')}${currentUrl.search}${currentUrl.hash}`;
            } else {
                newPath = `/${store.defaultLanguage}${currentUrl.search}${currentUrl.hash}`;
            }
        } catch (e) {
            console.warn("Error parsing URL from headers, falling back to root redirect:", e);
            newPath = `/${store.defaultLanguage}`;
        }
    } else {
        // Fallback if path cannot be determined
        newPath = `/${store.defaultLanguage}`;
    }
    console.warn(`Locale '${urlLocale}' not supported by store '${store.id}'. Redirecting to store's default language '${store.defaultLanguage}': ${newPath}`);
    return newPath;
}