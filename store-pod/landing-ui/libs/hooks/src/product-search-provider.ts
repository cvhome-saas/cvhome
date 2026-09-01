'use client'
import {useMemo} from 'react';
import {CategoryService} from '@store-front/services/category-service';
import {ContentService} from '@store-front/services/content-service';
import {ProductSearchService} from '@store-front/services/product-search-service';
import type {Category, SearchCapabilities, SearchHit, StoreContext} from '@store-front/types';
import {noopSearchProvider, type SearchProvider} from './use-search';

/**
 * The one search provider every theme uses.
 *
 * Products come from the catalog's suggest endpoint; categories and CMS pages come from navigation data the
 * shell has usually already fetched. Both, because a shopper typing "shoes" may want the product or the
 * department, and the two are cheap to offer together.
 *
 * This used to be copied into each of the twelve themes, each hard-coding `{text: false}`. That meant the
 * shell's capability flags never actually reached the browser — flipping text search on server-side would have
 * changed nothing a shopper could see. One provider, imported by all of them, is what makes the capability real.
 */

const MAX_PRODUCT_HITS = 6;
const MAX_NAV_HITS = 4;

function flatten(categories: Category[] | undefined, acc: Category[] = []): Category[] {
    for (const c of categories ?? []) {
        acc.push(c);
        flatten(c.children, acc);
    }
    return acc;
}

/**
 * The category tree and the site's pages, fetched once per store and language.
 *
 * Safe to hold for the life of the tab: it is the same navigation the header is already rendering, and it
 * changes when a merchant edits their shop, not while a shopper types.
 */
const navIndex = new Map<string, Promise<SearchHit[]>>();

async function loadNavIndex(ctx: StoreContext): Promise<SearchHit[]> {
    const key = `${ctx.store}:${ctx.locale}`;
    let pending = navIndex.get(key);
    if (!pending) {
        pending = Promise.all([CategoryService.getCategories(ctx), ContentService.getSite(ctx)]).then(([cats, site]) => {
            const hits: SearchHit[] = [];
            for (const c of flatten(cats?.content)) {
                if (!c.description || !c.visible) continue;
                hits.push({
                    kind: 'category',
                    id: `c-${c.id}`,
                    title: c.description.name,
                    href: `/category/${c.description.friendlyUrl}`,
                });
            }
            // CMS pages come off the site document: main-menu page entries plus the footer page list.
            const seen = new Set<string>();
            const menuPages = (site?.menus.main ?? [])
                .filter(n => n.kind === 'PAGE' && n.value)
                .map(n => ({slug: n.value as string, title: n.label, href: n.href}));
            for (const p of [...menuPages, ...(site?.footerPages ?? [])]) {
                if (seen.has(p.slug)) continue;
                seen.add(p.slug);
                hits.push({kind: 'page', id: `p-${p.slug}`, title: p.title, href: p.href});
            }
            return hits;
        });
        navIndex.set(key, pending);
        pending.catch(() => navIndex.delete(key));
    }
    return pending;
}

async function navHits(ctx: StoreContext, query: string): Promise<SearchHit[]> {
    try {
        const q = query.toLowerCase();
        return (await loadNavIndex(ctx)).filter(h => h.title.toLowerCase().includes(q)).slice(0, MAX_NAV_HITS);
    } catch {
        // A dropdown missing its category rows is worth less than a dropdown that fails outright.
        return [];
    }
}

async function productHits(ctx: StoreContext, query: string, signal?: AbortSignal): Promise<SearchHit[]> {
    const suggestions = await ProductSearchService.suggest(ctx, query, signal, MAX_PRODUCT_HITS);
    return suggestions.map(s => ({
        kind: 'product' as const,
        id: `p-${s.id}`,
        title: s.name,
        // A term that matched a combination variant's sku still suggests the one product — the
        // deep link lands the PDP with that variant preselected.
        href: `/product/${s.friendlyUrl}${s.matchedVariantSku ? `?sku=${encodeURIComponent(s.matchedVariantSku)}` : ''}`,
        imageUrl: s.imageUrl,
        subtitle: s.brand,
    }));
}

/** Products from the catalog, plus categories and pages from navigation. */
export const productSearchProvider: SearchProvider = {
    capabilities: {text: true, suggestions: true},
    async search(ctx, query, signal) {
        // Products lead: they are what a search box is for, and the nav rows are context beneath them.
        const [products, nav] = await Promise.all([productHits(ctx, query, signal), navHits(ctx, query)]);
        return {query, hits: [...products, ...nav]};
    },
};

/**
 * Categories and pages only. Kept for deployments pointed at a catalog without the search endpoint — the
 * capability flags are what a theme branches on, so this degrades the UI honestly rather than failing.
 */
export const navigationSearchProvider: SearchProvider = {
    capabilities: {text: false, suggestions: true},
    async search(ctx, query) {
        return {query, hits: await navHits(ctx, query)};
    },
};

/**
 * The provider matching what the shell said this deployment can do.
 *
 * A theme calls this instead of choosing for itself, so "can we search products" is answered in one place. The
 * capabilities arrive in `LayoutData`, put there by the shell's `getSearchCapabilities()`. They are decided
 * server-side and the providers are only ever constructed here, in the browser: this module is `'use client'`,
 * and a server importing it would get a client reference proxy rather than the objects.
 */
export function useSearchProvider(capabilities: SearchCapabilities): SearchProvider {
    return useMemo(() => {
        if (capabilities.text) return productSearchProvider;
        if (capabilities.suggestions) return navigationSearchProvider;
        return noopSearchProvider;
    }, [capabilities.text, capabilities.suggestions]);
}
