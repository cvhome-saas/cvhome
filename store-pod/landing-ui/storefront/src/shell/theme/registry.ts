import type {ThemeDefinition} from '@store-front/theme';
import type {RegisteredThemeId} from './theme-id';

/**
 * Every theme by id, loaded on demand. Only `/api/theme-manifest` uses it: it answers for whichever theme the
 * request resolves to, and it is server-only, so it ships nothing to a browser. Pages must not import it — a route
 * that reaches this map puts every theme's client code in its CSS and JS; each route tree imports its own theme
 * statically instead (`app/(storefront)/t/<id>`, written by `scripts/theme-routes.mjs`). Managed by
 * `scripts/new-theme.mjs` — keep one line per theme between the markers.
 */
export const themeLoaders = {
    // @themes:start
    starter: () => import('@store-front/theme-starter'),
    'beauty': () => import('@store-front/theme-beauty'),
    'fashion': () => import('@store-front/theme-fashion'),
    'basic': () => import('@store-front/theme-basic'),
    'grocery': () => import('@store-front/theme-grocery'),
    'pink': () => import('@store-front/theme-pink'),
    'hunger': () => import('@store-front/theme-hunger'),
    'furniture': () => import('@store-front/theme-furniture'),
    'glasses': () => import('@store-front/theme-glasses'),
    'cosmetics': () => import('@store-front/theme-cosmetics'),
    'sports': () => import('@store-front/theme-sports'),
    'jewellery': () => import('@store-front/theme-jewellery'),
    // @themes:end
} satisfies Record<RegisteredThemeId, () => Promise<{ default: ThemeDefinition }>>;
