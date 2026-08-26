import 'server-only';
import {cache} from 'react';
import {CategoryService} from '@store-front/services/category-service';
import type {LayoutData} from '@store-front/theme';
import type {NavPage} from '@store-front/types';
import {getStore, getStoreContext} from '@/shell/request/store-context';
import {getSearchProvider} from '@/shell/search';
import {bannerAsAnnouncement, linkAsNavPage, loadSite} from './site';

/**
 * Everything the theme's Root layout needs, fetched in parallel and memoised per request.
 *
 * Content comes from the storefront `site` document: menus and policies as they are, and — for themes that
 * render the flat `pages` list — the main-menu pages and footer pages as `NavPage` links.
 */
export const loadLayoutData = cache(async (): Promise<LayoutData> => {
    const ctx = await getStoreContext();
    const [store, categories, site] = await Promise.all([
        getStore(),
        CategoryService.getCategories(ctx),
        loadSite(),
    ]);
    const menuPages: NavPage[] = site.menus.main
        .filter(n => n.kind === 'PAGE' && n.value)
        .map(n => linkAsNavPage({slug: n.value as string, title: n.label, href: n.href, type: 'page'}, true));
    const footerPages: NavPage[] = site.footerPages
        .filter(p => !menuPages.some(m => m.code === p.slug))
        .map(p => linkAsNavPage(p, false));
    return {
        store,
        categories: (categories?.content ?? []).filter(c => c.visible !== false),
        pages: [...menuPages, ...footerPages],
        announcement: site.announcement ? bannerAsAnnouncement(site.announcement) : undefined,
        menus: site.menus,
        policies: site.policies,
        branding: site.branding,
        socialLinks: site.socialLinks,
        search: getSearchProvider().capabilities,
    };
});
