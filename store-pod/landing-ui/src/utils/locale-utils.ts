import {FALLBACK_LOCALE, STORE_DEFAULT_LOCALE_COOKIE_NAME} from "@/types/constant";

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
