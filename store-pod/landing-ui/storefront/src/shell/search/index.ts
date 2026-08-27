import {FULL_SEARCH, NAVIGATION_SEARCH, NO_SEARCH, type SearchCapabilities} from '@store-front/types';

/**
 * What this deployment's search can answer, decided server-side and handed to themes in `LayoutData`.
 *
 * Capabilities, not a provider. The providers live in a `'use client'` module, and importing one here would
 * hand the server a client reference proxy rather than the object — reading `.capabilities` off it silently
 * yields `undefined`, and the first thing to break is every page that renders a search box. The server settles
 * what is possible; `useSearchProvider(capabilities)` picks the matching provider in the browser.
 *
 * The catalog has a full-text endpoint, so the default is the full set. The alternatives stay because a
 * capability is a claim about a deployment, not about the code: `navigation` for a pod whose catalog predates
 * the endpoint, `none` to turn the box off entirely.
 */
export function getSearchCapabilities(): SearchCapabilities {
    switch (process.env.NEXT_PUBLIC_STOREFRONT_SEARCH_PROVIDER) {
        case 'none':
            return NO_SEARCH;
        case 'navigation':
        case 'category-nav':
            return NAVIGATION_SEARCH;
        case 'product':
        default:
            return FULL_SEARCH;
    }
}
