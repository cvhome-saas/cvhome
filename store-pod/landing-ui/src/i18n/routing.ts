import {defineRouting} from 'next-intl/routing';
import {locales} from "@/types/constant";
import {getLocaleFromCookie, setLocaleCookie} from "@/utils/locale-utils";


export const routing = defineRouting({
    locales,
    defaultLocale: (() => {
        const locale = getLocaleFromCookie();
        if (!locale) {
            return setLocaleCookie();
        }
        return locale;
    })(),
    localeCookie: true,
    localeDetection: false,
});