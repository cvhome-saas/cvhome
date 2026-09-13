import type {Metadata} from 'next';
import {loadProduct} from '@/shell/loaders/product';
import {pageMetadata} from '@/shell/seo/metadata';
import {themed, type ThemeSource} from './theme-source';

type Props = { params: Promise<{ url: string }> };

export async function generateMetadata({params}: Props): Promise<Metadata> {
    const {url} = await params;
    // Metadata is streamed in Next 16: a notFound()/error thrown here would surface as a generic error
    // instead of a 404. Let the page decide the status; metadata for a missing entity is irrelevant.
    try {
        const {product} = await loadProduct(url);
        return pageMetadata(product.description?.title || product.description?.name, product.description?.metaDescription);
    } catch {
        return {};
    }
}

/** No Suspense here on purpose: a notFound()/error must set the real HTTP status (SEO). */
export function productPage(theme: ThemeSource) {
    return async function ProductPage({params}: Props) {
        const {url} = await params;
        const [{theme: resolved, ctx}, data] = await Promise.all([themed(theme), loadProduct(url)]);
        return <resolved.pages.Product ctx={ctx} data={data}/>;
    };
}
