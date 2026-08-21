import {getRequestConfig} from 'next-intl/server';
import {hasLocale} from 'next-intl';
import {routing} from '@store-front/i18n/routing';

export default getRequestConfig(async ({requestLocale}) => {
    const requested = await requestLocale;
    const locale = hasLocale(routing.locales, requested) ? requested : routing.defaultLocale;

    return {
        locale,
        // Locales are SHARED across every theme: landing-ui/locales/{en,ar,es,fr,ru}.json
        messages: (await import(`../../../locales/${locale}.json`)).default,
        getMessageFallback: ({key}) => `-*${key}*-`,
    };
});
