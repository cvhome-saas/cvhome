import 'server-only';
import {cache} from 'react';
import {CategoryService} from '@store-front/services/category-service';
import type {LayoutData} from '@store-front/theme';
import type {Page} from '@store-front/types';
import {getStore, getStoreContext} from '@/shell/request/store-context';
import {getSearchProvider} from '@/shell/search';
import {bannerAsBox, linkAsPage, loadSite} from './site';

/**
 * Everything the theme's Root layout needs, fetched in parallel and memoised per request.
 *
 * Content comes from the storefront `site` document: menus and policies as they are, and — for themes that
 * still read the legacy `pages` list — the main-menu pages and footer pages folded into `Page` objects.
 */
export const loadLayoutData = cache(async (): Promise<LayoutData> => {
    const ctx = await getStoreContext();
    const [store, categories, site] = await Promise.all([
        getStore(),
        CategoryService.getCategories(ctx),
        loadSite(),
    ]);
    const menuPages: Page[] = site.menus.main
        .filter(n => n.kind === 'PAGE' && n.value)
        .map(n => linkAsPage({slug: n.value as string, title: n.label, href: n.href, type: 'page'}, ctx.locale, true));
    const footerPages: Page[] = site.footerPages
        .filter(p => !menuPages.some(m => m.code === p.slug))
        .map(p => linkAsPage(p, ctx.locale, false));
    return {
        store,
        categories: (categories?.content ?? []).filter(c => c.visible !== false),
        pages: [...menuPages, ...footerPages],
        announcement: site.announcement ? bannerAsBox(site.announcement, ctx.locale) : undefined,
        menus: site.menus,
        policies: site.policies,
        search: getSearchProvider().capabilities,
    };
});
