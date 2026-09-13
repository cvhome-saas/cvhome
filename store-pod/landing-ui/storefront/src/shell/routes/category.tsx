import type {Metadata} from 'next';
import {loadCategory} from '@/shell/loaders/category';
import {pageMetadata} from '@/shell/seo/metadata';
import {themed, type ThemeSource} from './theme-source';

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
export function categoryPage(theme: ThemeSource) {
    return async function CategoryPage({params, searchParams}: Props) {
        const {url} = await params;
        const [{theme: resolved, ctx}, data] = await Promise.all([themed(theme), loadCategory(url, await search(searchParams))]);
        return <resolved.pages.Category ctx={ctx} data={data}/>;
    };
}
