import type {Metadata} from 'next';
import type {ThemeDefinition} from '@store-front/theme';
import {loadBlogPost} from '@/shell/loaders/blog';
import {pageMetadata} from '@/shell/seo/metadata';
import {loadPageContext} from '@/shell/loaders/page-context';

type Props = { params: Promise<{ slug: string }>; searchParams: Promise<{ preview?: string }> };

export async function generateMetadata({params, searchParams}: Props): Promise<Metadata> {
    const [{slug}, {preview}] = await Promise.all([params, searchParams]);
    try {
        const {post} = await loadBlogPost(slug, preview);
        return pageMetadata(post.seo?.metaTitle || post.title, post.seo?.metaDescription || post.excerpt || undefined, post.seo);
    } catch {
        return {};
    }
}

/** No Suspense here on purpose: a notFound()/error must set the real HTTP status (SEO). */
export function blogPostPage(theme: ThemeDefinition) {
    return async function BlogPostPage({params, searchParams}: Props) {
        const [{slug}, {preview}] = await Promise.all([params, searchParams]);
        const [ctx, data] = await Promise.all([loadPageContext(theme), loadBlogPost(slug, preview)]);
        return <theme.pages.BlogPost ctx={ctx} data={data}/>;
    };
}
