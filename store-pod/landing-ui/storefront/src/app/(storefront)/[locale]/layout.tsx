import '../../globals.css';
import type {Metadata} from 'next';
import type {ReactNode} from 'react';
import {headers} from 'next/headers';
import {redirect} from 'next/navigation';
import {NextIntlClientProvider} from 'next-intl';
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
    const [theme, storeContext] = await Promise.all([getTheme(), getStoreContext()]);

    let store;
    try {
        store = await getStore();
    } catch (e) {
        // The whole storefront renders from the store record; without it this domain has no store.
        if (isApiError(e) && (e.category === 'NOT_FOUND' || e.category === 'FORBIDDEN')) redirect('/store-not-found');
        throw e;
    }

    if (!localSupported(locale, store)) {
        redirectToSupportedLang(store, await headers(), locale);
    }

    const data = await loadLayoutData();
    const merchant = resolveMerchantTokens(theme, await getColorThemeRequest(store));
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
