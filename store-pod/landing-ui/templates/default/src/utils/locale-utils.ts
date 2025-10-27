import {Store} from "@/types/store";
import type {ReadonlyHeaders} from "next/dist/server/web/spec-extension/adapters/headers";
import {redirect} from 'next/navigation';

export function buildDefaultLangRedirectionUrl(store: Store, headers: ReadonlyHeaders, urlLocale: string): string {
    let newPath: string;
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

export function redirectToSupportedLang(store: Store, headers: ReadonlyHeaders, urlLocale: string) {
    redirect(buildDefaultLangRedirectionUrl(store, headers, urlLocale));
}