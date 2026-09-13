import type {Metadata} from 'next';
import type {ThemeDefinition} from '@store-front/theme';
import {loadContent} from '@/shell/loaders/content';
import {pageMetadata} from '@/shell/seo/metadata';
import {loadPageContext} from '@/shell/loaders/page-context';

type Props = { params: Promise<{ url: string }>; searchParams: Promise<{ preview?: string }> };

export async function generateMetadata({params, searchParams}: Props): Promise<Metadata> {
    const [{url}, {preview}] = await Promise.all([params, searchParams]);
    // Metadata is streamed in Next 16: a notFound()/error thrown here would surface as a generic error
    // instead of a 404. Let the page decide the status; metadata for a missing entity is irrelevant.
    try {
        const {page, seo} = await loadContent(url, preview);
        return pageMetadata(seo?.metaTitle || page.title, seo?.metaDescription ?? undefined, seo);
    } catch {
        return {};
    }
}

/** No Suspense here on purpose: a notFound()/error must set the real HTTP status (SEO). */
export function contentPage(theme: ThemeDefinition) {
    return async function ContentPage({params, searchParams}: Props) {
        const [{url}, {preview}] = await Promise.all([params, searchParams]);
        const [ctx, data] = await Promise.all([loadPageContext(theme), loadContent(url, preview)]);
        return <theme.pages.Content ctx={ctx} data={data}/>;
    };
}
