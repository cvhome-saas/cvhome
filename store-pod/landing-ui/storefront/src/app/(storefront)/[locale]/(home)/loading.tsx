import {getTheme} from '@/shell/theme/get-theme';

/** Home-only skeleton. Lives in its own route group so the Suspense boundary does NOT wrap child routes
 *  (a loading.tsx at [locale]/ would turn every notFound() below it into a streamed 200). */
export default async function Loading() {
    const theme = await getTheme();
    return <theme.states.PageSkeleton.home/>;
}
