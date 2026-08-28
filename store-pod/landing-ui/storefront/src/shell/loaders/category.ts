import 'server-only';
import {cache} from 'react';
import {notFound} from 'next/navigation';
import {getTranslations} from 'next-intl/server';
import {CategoryService} from '@store-front/services/category-service';
import {ProductCategory} from '@store-front/services/product-category';
import {isApiError, type ListingQuery, type ProductListingPage} from '@store-front/types';
import type {CategoryData} from '@store-front/theme';
import {getStoreContext} from '@/shell/request/store-context';

export const loadCategory = cache(async (url: string, query: ListingQuery): Promise<CategoryData> => {
    const ctx = await getStoreContext();
    let category;
    try {
        category = await CategoryService.getCategory(ctx, url);
    } catch (e) {
        if (isApiError(e) && e.category === 'NOT_FOUND') notFound();
        throw e;
    }
    if (!category) notFound();
    const t = await getTranslations('COMMON');
    const [initial, facets] = await Promise.all([
        ProductCategory.getProducts(ctx, query, category.id).catch((e): ProductListingPage => {
            // The listing hook retries client-side and shows the error state; the page itself still renders.
            console.warn('Category listing failed server-side, deferring to client:', e);
            return {totalPages: 0, pageNumber: 0, totalElements: 0, recordsFiltered: 0, content: undefined, productGroup: undefined};
        }),
        ProductCategory.getFacets(ctx, category.id),
    ]);
    const crumbs = [{id: 'home', name: t('HOME'), href: '/'}];
    if (category.parent?.description) {
        crumbs.push({id: String(category.parent.id), name: category.parent.description.name, href: `/category/${category.parent.description.friendlyUrl}`});
    }
    crumbs.push({id: String(category.id), name: category.description.name, href: `/category/${category.description.friendlyUrl}`});
    return {category, breadcrumbs: crumbs, initial, query, facets};
});
