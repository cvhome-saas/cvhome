import {defineRouting} from 'next-intl/routing';
import {locales} from '@store-front/types/constant';

export const routing = defineRouting({
    locales,
    defaultLocale: 'en',
    localeCookie: true,
    localeDetection: false,
});
