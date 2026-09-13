import type {Metadata} from 'next';
import {getTheme} from '@/shell/theme/get-theme';
import {loadCategory} from '@/shell/loaders/category';
import {loadPageContext} from '@/shell/loaders/page-context';
import {pageMetadata} from '@/shell/seo/metadata';

type Props = { params: Promise<{ url: string }>; searchParams: Promise<Record<string, string | string[] | undefined>> };

/** The listing query as a string, so the metadata and the page hit the same `loadCategory` memo. */
async function search(searchParams: Props['searchParams']): Promise<string> {
    const sp = await searchParams;
    const usp = new URLSearchParams();
    for (const [k, v] of Object.entries(sp)) if (typeof v === 'string') usp.set(k, v);
    return usp.toString();
}

export async function generateMetadata({params, searchParams}: Props): Promise<Metadata> {
    const {url} = await params;
    // Metadata is streamed in Next 16: a notFound()/error thrown here would surface as a generic error
    // instead of a 404. Let the page decide the status; metadata for a missing entity is irrelevant.
    try {
        const data = await loadCategory(url, await search(searchParams));
        return pageMetadata(data.category.description?.title || data.category.description?.name, data.category.description?.metaDescription);
    } catch {
        return {};
    }
}

/** No Suspense here on purpose: a notFound()/error must set the real HTTP status (SEO). */
export default async function CategoryPage({params, searchParams}: Props) {
    const {url} = await params;
    const [theme, ctx, data] = await Promise.all([getTheme(), loadPageContext(), loadCategory(url, await search(searchParams))]);
    return <theme.pages.Category ctx={ctx} data={data}/>;
}
