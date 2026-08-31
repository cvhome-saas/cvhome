import '../../globals.css';
import type {Metadata} from 'next';
import type {ReactNode} from 'react';
import {headers} from 'next/headers';
import {notFound, redirect} from 'next/navigation';
import {NextIntlClientProvider} from 'next-intl';
import {routing} from '@store-front/i18n/routing';
import {getDirection} from '@store-front/i18n/direction';
import {localSupported, redirectToSupportedLang} from '@store-front/services/locale-utils';
import {isApiError} from '@store-front/types';
import {getStore, getStoreContext} from '@/shell/request/store-context';
import {getTheme} from '@/shell/theme/get-theme';
import {ThemeClientStates} from '@/shell/theme/theme-client-states';
import {getColorThemeRequest, resolveMerchantTokens} from '@/shell/tokens/merchant-tokens';
import {loadLayoutData} from '@/shell/loaders/layout';
import {loadStoreMetadata} from '@/shell/seo/metadata';

export async function generateMetadata(): Promise<Metadata> {
    return loadStoreMetadata();
}

export default async function StorefrontLayout({children, params}: { children: ReactNode; params: Promise<{ locale: string }> }) {
    const {locale} = await params;

    // `[locale]` matches any single segment, and proxy.ts deliberately lets paths with a file extension
    // past next-intl, so /llms.txt, /ads.txt and every scanner probe land here as a "locale". Reject
    // anything that is not one of the app's locales up front: without this a bogus path costs a store
    // fetch and a full storefront render before redirectToSupportedLang() sends it to the default
    // language — Lighthouse measured 383 KB for one such probe.
    if (!(routing.locales as readonly string[]).includes(locale)) notFound();

    // Start the independent storefront reads together. React's request cache deduplicates the store read
    // shared by getTheme(), loadLayoutData(), and getStore().
    const themePromise = getTheme();
    const storeContextPromise = getStoreContext();
    const storePromise = getStore();
    const layoutDataPromise = loadLayoutData();
    // A redirect can end this render before the eager reads are awaited; attach handlers so a peer
    // service failure cannot become an unhandled rejection during that early exit.
    void storePromise.catch(() => undefined);
    void layoutDataPromise.catch(() => undefined);
    const [theme, storeContext] = await Promise.all([themePromise, storeContextPromise]);

    let store;
    try {
        store = await storePromise;
    } catch (e) {
        // The whole storefront renders from the store record; without it this domain has no store.
        if (isApiError(e) && (e.category === 'NOT_FOUND' || e.category === 'FORBIDDEN')) redirect('/store-not-found');
        throw e;
    }

    if (!localSupported(locale, store)) {
        redirectToSupportedLang(store, await headers(), locale);
    }

    const [data, colorThemeRequest] = await Promise.all([layoutDataPromise, getColorThemeRequest(store)]);
    const merchant = resolveMerchantTokens(theme, colorThemeRequest);
    const dir = getDirection(locale);
    const ctx = {store, storeContext, locale, dir, layout: theme.layout.config};

    return (
        <html
            lang={locale}
            dir={dir}
            data-theme={theme.id}
            data-theme-version={theme.version}
            data-color-scheme={merchant.scheme}
            data-color-theme={merchant.preset}
            className={theme.fonts.variables}
            style={merchant.style}
        >
        <body className="flex min-h-dvh flex-col">
        <NextIntlClientProvider>
            <ThemeClientStates states={{ErrorState: theme.states.ErrorState, EmptyState: theme.states.EmptyState, Redirecting: theme.states.Redirecting}}>
                <theme.layout.Root ctx={ctx} data={data}>{children}</theme.layout.Root>
            </ThemeClientStates>
        </NextIntlClientProvider>
        </body>
        </html>
    );
}
