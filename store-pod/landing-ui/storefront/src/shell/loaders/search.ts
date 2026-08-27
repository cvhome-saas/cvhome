import 'server-only';
import {cache} from 'react';
import {ProductSearchService} from '@store-front/services/product-search-service';
import {EMPTY_FACETS, emptySearchPage, type ProductSearchPage, type ProductSearchQuery} from '@store-front/types';
import type {SearchData} from '@store-front/theme';
import {getStoreContext} from '@/shell/request/store-context';

export const loadSearch = cache(async (query: ProductSearchQuery): Promise<SearchData> => {
    const ctx = await getStoreContext();
    const initial = await ProductSearchService.search(ctx, query).catch((e): ProductSearchPage => {
        // The search hook retries client-side and shows the error state; the page itself still renders, with
        // its box and its filters, so a shopper can try again without a browser error page.
        console.warn('Product search failed server-side, deferring to client:', e);
        return emptySearchPage();
    });
    return {
        query,
        initial,
        facets: initial.facets ?? EMPTY_FACETS,
        didYouMean: initial.didYouMean,
        // Only worth telling a shopper about when it is not the language they are reading the shop in.
        fallbackLanguage: initial.language && initial.language !== ctx.locale ? initial.language : undefined,
    };
});
