'use client'
import type {SearchProvider} from '@store-front/hooks/use-search';
import {CategoryService} from '@store-front/services/category-service';
import {ContentService} from '@store-front/services/content-service';
import type {Category, SearchHit, StoreContext} from '@store-front/types';

/**
 * Client-side suggestion provider over categories and CMS pages (the same data the nav already shows).
 * Mirrors the shell's `categoryNavSearchProvider`; lives in the theme so the bundle boundary is explicit.
 */
function flatten(categories: Category[] | undefined, acc: Category[] = []): Category[] {
    for (const c of categories ?? []) {
        acc.push(c);
        flatten(c.children, acc);
    }
    return acc;
}

const index = new Map<string, Promise<SearchHit[]>>();

async function load(ctx: StoreContext): Promise<SearchHit[]> {
    const key = `${ctx.store}:${ctx.locale}`;
    let pending = index.get(key);
    if (!pending) {
        pending = Promise.all([CategoryService.getCategories(ctx), ContentService.getSite(ctx)]).then(([cats, site]) => {
            const hits: SearchHit[] = [];
            for (const c of flatten(cats?.content)) if (c.description && c.visible !== false)
                hits.push({kind: 'category', id: `c-${c.id}`, title: c.description.name, href: `/category/${c.description.friendlyUrl}`});
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
        index.set(key, pending);
        pending.catch(() => index.delete(key));
    }
    return pending;
}

export const provider: SearchProvider = {
    capabilities: {text: false, suggestions: true},
    async search(ctx, query) {
        const q = query.toLowerCase();
        const all = await load(ctx);
        return {query, hits: all.filter(h => h.title.toLowerCase().includes(q)).slice(0, 8)};
    },
};
