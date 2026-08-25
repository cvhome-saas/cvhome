import 'server-only';
import {cache} from 'react';
import {getLocale} from 'next-intl/server';
import {getDirection} from '@store-front/i18n/direction';
import type {PageContext} from '@store-front/theme';
import {getStore, getStoreContext} from '@/shell/request/store-context';
import {getTheme} from '@/shell/theme/get-theme';

/** The `ctx` every theme page receives. */
export const loadPageContext = cache(async (): Promise<PageContext> => {
    const [store, storeContext, locale, theme] = await Promise.all([getStore(), getStoreContext(), getLocale(), getTheme()]);
    return {store, storeContext, locale, dir: getDirection(locale), layout: theme.layout.config};
});
