import 'server-only';
import {cache} from 'react';
import {CategoryService} from '@store-front/services/category-service';
import {ContentService} from '@store-front/services/content-service';
import type {LayoutData} from '@store-front/theme';
import {getStore, getStoreContext} from '@/shell/request/store-context';
import {getSearchProvider} from '@/shell/search';

/** Everything the theme's Root layout needs, fetched in parallel and memoised per request. */
export const loadLayoutData = cache(async (): Promise<LayoutData> => {
    const ctx = await getStoreContext();
    const [store, categories, pages, announcement] = await Promise.all([
        getStore(),
        CategoryService.getCategories(ctx),
        ContentService.getContents(ctx),
        ContentService.getBox(ctx, 'header-message'),
    ]);
    return {
        store,
        categories: (categories?.content ?? []).filter(c => c.visible !== false),
        pages: (pages?.content ?? []).filter(p => p.visible),
        announcement: announcement?.visible ? announcement : undefined,
        search: getSearchProvider().capabilities,
    };
});
