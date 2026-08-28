import {getTheme} from '@/shell/theme/get-theme';

/**
 * Safe here, unlike on the category route: search has no dynamic segment and every query is a valid page, so
 * there is no notFound() for a Suspense boundary to turn into a streamed 200.
 *
 * The search skeleton is optional in the contract; a theme without one gets its category skeleton, which is the
 * same furniture — a rail, a toolbar and a grid.
 */
export default async function Loading() {
    const theme = await getTheme();
    const Skeleton = theme.states.PageSkeleton.search ?? theme.states.PageSkeleton.category;
    return <Skeleton/>;
}
