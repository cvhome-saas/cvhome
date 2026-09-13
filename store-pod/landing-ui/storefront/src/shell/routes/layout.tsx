import type {Metadata} from 'next';
import type {ReactNode} from 'react';
import {preconnect} from 'react-dom';
import {headers} from 'next/headers';
import {notFound, redirect} from 'next/navigation';
import {NextIntlClientProvider} from 'next-intl';
import {routing} from '@store-front/i18n/routing';
import {getDirection} from '@store-front/i18n/direction';
import {localSupported, redirectToSupportedLang} from '@store-front/services/locale-utils';
import {isApiError} from '@store-front/types';
import {getStore, getStoreContext} from '@/shell/request/store-context';
import {ThemeClientStates} from '@/shell/theme/theme-client-states';
import {getColorThemeRequest, resolveMerchantTokens} from '@/shell/tokens/merchant-tokens';
import {loadLayoutData} from '@/shell/loaders/layout';
import {loadStoreMetadata} from '@/shell/seo/metadata';
import type {ThemeSource} from './theme-source';

export async function generateMetadata(): Promise<Metadata> {
    return loadStoreMetadata();
}

/**
 * The `[locale]` root layout: `<html>` with the theme's fonts and tokens, the merchant colours, the theme's Root. The
 * stylesheet is imported by each tree's layout.tsx, ahead of its theme (see scripts/theme-routes.mjs).
 */
export function storefrontLayout(theme: ThemeSource) {
    return async function StorefrontLayout({children, params}: { children: ReactNode; params: Promise<{ locale: string }> }) {
        const {locale} = await params;

        // `[locale]` matches any single segment, and proxy.ts deliberately lets paths with a file extension
        // past next-intl, so /llms.txt, /ads.txt and every scanner probe land here as a "locale". Reject
        // anything that is not one of the app's locales up front: without this a bogus path costs a store
        // fetch and a full storefront render before redirectToSupportedLang() sends it to the default
        // language — Lighthouse measured 383 KB for one such probe.
        if (!(routing.locales as readonly string[]).includes(locale)) notFound();

        // Start the independent storefront reads together. React's request cache deduplicates the store read
        // shared by the theme, loadLayoutData(), and getStore().
        const themePromise = theme();
        const storeContextPromise = getStoreContext();
        const storePromise = getStore();
        const layoutDataPromise = loadLayoutData();
        // A redirect can end this render before the eager reads are awaited; attach handlers so a peer
        // service failure cannot become an unhandled rejection during that early exit.
        void storePromise.catch(() => undefined);
        void layoutDataPromise.catch(() => undefined);
        const [resolved, storeContext] = await Promise.all([themePromise, storeContextPromise]);

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

        // CDN mode (see start.mjs): fonts and scripts load from the static-assets host — open its
        // connections while the HTML streams instead of paying DNS+TLS when the first asset is found.
        // Fonts fetch with `crossorigin`, scripts without; each needs its own warmed connection.
        const staticAssetsBase = process.env.STATIC_ASSETS_BASE_URL;
        if (staticAssetsBase) {
            preconnect(staticAssetsBase);
            preconnect(staticAssetsBase, {crossOrigin: 'anonymous'});
        }

        const [data, colorThemeRequest] = await Promise.all([layoutDataPromise, getColorThemeRequest(store)]);
        const merchant = resolveMerchantTokens(resolved, colorThemeRequest);
        const dir = getDirection(locale);
        const ctx = {store, storeContext, locale, dir, layout: resolved.layout.config};
        const states = {
            ErrorState: resolved.states.ErrorState,
            EmptyState: resolved.states.EmptyState,
            Redirecting: resolved.states.Redirecting,
        };

        return (
            <html
                lang={locale}
                dir={dir}
                data-theme={resolved.id}
                data-theme-version={resolved.version}
                data-color-scheme={merchant.scheme}
                data-color-theme={merchant.preset}
                className={resolved.fonts.variables}
                style={merchant.style}
            >
            <body className="flex min-h-dvh flex-col">
            <NextIntlClientProvider>
                <ThemeClientStates states={states}>
                    <resolved.layout.Root ctx={ctx} data={data}>{children}</resolved.layout.Root>
                </ThemeClientStates>
            </NextIntlClientProvider>
            </body>
            </html>
        );
    };
}
