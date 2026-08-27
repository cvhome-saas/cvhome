import type {SearchProvider} from '@store-front/hooks/use-search';
import {noopSearchProvider} from '@store-front/hooks/use-search';
import {navigationSearchProvider, productSearchProvider} from '@store-front/hooks/product-search-provider';

/**
 * Which search provider the storefront ships.
 *
 * The catalog has a full-text endpoint now, so the default searches products. The alternatives stay because a
 * capability is a claim about a deployment, not about the code: `navigation` for a pod whose catalog predates
 * the endpoint, `none` to turn the box off entirely. Themes branch on `capabilities`, so each of these renders
 * honestly rather than promising a search that answers nothing.
 */
export function getSearchProvider(): SearchProvider {
    switch (process.env.NEXT_PUBLIC_STOREFRONT_SEARCH_PROVIDER) {
        case 'none':
            return noopSearchProvider;
        case 'navigation':
        case 'category-nav':
            return navigationSearchProvider;
        case 'product':
        default:
            return productSearchProvider;
    }
}
