import type {SearchProvider} from '@store-front/hooks/use-search';
import {noopSearchProvider} from '@store-front/hooks/use-search';
import {categoryNavSearchProvider} from './category-nav-search-provider';

/**
 * Which search provider the storefront ships. Today there is no catalog text-search endpoint, so the
 * default is suggestions-only over navigation data. When the backend adds text search, add a provider
 * here and flip the default — themes already branch on `capabilities`.
 */
export function getSearchProvider(): SearchProvider {
    switch (process.env.NEXT_PUBLIC_STOREFRONT_SEARCH_PROVIDER) {
        case 'none':
            return noopSearchProvider;
        case 'category-nav':
        default:
            return categoryNavSearchProvider;
    }
}
