import type {ThemeSource} from './theme-source';

/** Which skeleton a route's `loading.tsx` shows. */
type SkeletonKind = 'home' | 'checkout' | 'customer' | 'order' | 'search';

/**
 * A route's loading screen: the theme's skeleton for it.
 *
 * `home` lives in its own route group so the Suspense boundary does NOT wrap child routes (a loading.tsx at
 * [locale]/ would turn every notFound() below it into a streamed 200). `search` is safe unlike the category route:
 * it has no dynamic segment and every query is a valid page. The search skeleton is optional in the contract; a theme
 * without one gets its category skeleton, which is the same furniture — a rail, a toolbar and a grid.
 */
export function loadingScreen(theme: ThemeSource, kind: SkeletonKind) {
    return async function Loading() {
        const {states} = await theme();
        const Skeleton = kind === 'search'
            ? states.PageSkeleton.search ?? states.PageSkeleton.category
            : states.PageSkeleton[kind];
        return <Skeleton/>;
    };
}
