import createMiddleware from 'next-intl/middleware';
import {localePrefix, locales} from './navigation';

export default createMiddleware({
    // A list of all locales that are supported
    defaultLocale: 'en',
    localePrefix,
    locales
});

export const config = {
    // Match only internationalized pathnames
    matcher: ['/', '/(fr|en)/:path*']
};