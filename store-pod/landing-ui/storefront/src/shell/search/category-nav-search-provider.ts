import type {SearchProvider} from '@store-front/hooks/use-search';
import {CategoryService} from '@store-front/services/category-service';
import {ContentService} from '@store-front/services/content-service';
import type {Category, SearchHit, StoreContext} from '@store-front/types';

function flatten(categories: Category[] | undefined, acc: Category[] = []): Category[] {
    for (const c of categories ?? []) {
        acc.push(c);
        flatten(c.children, acc);
    }
    return acc;
}

const cache = new Map<string, Promise<SearchHit[]>>();

async function loadIndex(ctx: StoreContext): Promise<SearchHit[]> {
    const key = `${ctx.store}:${ctx.locale}`;
    let pending = cache.get(key);
    if (!pending) {
        pending = Promise.all([CategoryService.getCategories(ctx), ContentService.getContents(ctx)]).then(([cats, pages]) => {
            const hits: SearchHit[] = [];
            for (const c of flatten(cats?.content)) {
                if (!c.description || !c.visible) continue;
                hits.push({kind: 'category', id: `c-${c.id}`, title: c.description.name, href: `/category/${c.description.friendlyUrl}`});
            }
            for (const p of pages?.content ?? []) {
                if (!p.description || !p.visible) continue;
                hits.push({kind: 'page', id: `p-${p.id}`, title: p.description.name, href: `/content/${p.description.friendlyUrl}`});
            }
            return hits;
        });
        cache.set(key, pending);
        pending.catch(() => cache.delete(key));
    }
    return pending;
}

/** Instant suggestions over categories and CMS pages. Honest about not searching products. */
export const categoryNavSearchProvider: SearchProvider = {
    capabilities: {text: false, suggestions: true},
    async search(ctx, query) {
        const q = query.toLowerCase();
        const index = await loadIndex(ctx);
        return {query, hits: index.filter(h => h.title.toLowerCase().includes(q)).slice(0, 8)};
    },
};
