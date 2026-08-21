import type {Metadata} from 'next';
import {getTheme} from '@/shell/theme/get-theme';
import {loadContent} from '@/shell/loaders/content';
import {loadPageContext} from '@/shell/loaders/page-context';
import {pageMetadata} from '@/shell/seo/metadata';

type Props = { params: Promise<{ url: string }> };

export async function generateMetadata({params}: Props): Promise<Metadata> {
    const {url} = await params;
    // Metadata is streamed in Next 16: a notFound()/error thrown here would surface as a generic error
    // instead of a 404. Let the page decide the status; metadata for a missing entity is irrelevant.
    try {
        const {page} = await loadContent(url);
        return pageMetadata(page.description?.title || page.description?.name, page.description?.metaDescription);
    } catch {
        return {};
    }
}

/** No Suspense here on purpose: a notFound()/error must set the real HTTP status (SEO). */
export default async function ContentPage({params}: Props) {
    const {url} = await params;
    const [theme, ctx, data] = await Promise.all([getTheme(), loadPageContext(), loadContent(url)]);
    return <theme.pages.Content ctx={ctx} data={data}/>;
}
