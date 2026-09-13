import 'server-only';
import {cache} from 'react';
import {getLocale} from 'next-intl/server';
import {getDirection} from '@store-front/i18n/direction';
import type {PageContext, ThemeDefinition} from '@store-front/theme';
import {getStore, getStoreContext} from '@/shell/request/store-context';

/**
 * The `ctx` every theme page receives. The theme is an argument, not resolved here, so a route tree that binds
 * its theme statically never reaches the registry of every theme. Memoised per request and per theme object.
 */
export const loadPageContext = cache(async (theme: ThemeDefinition): Promise<PageContext> => {
    const [store, storeContext, locale] = await Promise.all([getStore(), getStoreContext(), getLocale()]);
    return {store, storeContext, locale, dir: getDirection(locale), layout: theme.layout.config};
});
