import type {Metadata} from 'next';
import {getTheme} from '@/shell/theme/get-theme';
import {loadBlogPost} from '@/shell/loaders/blog';
import {loadPageContext} from '@/shell/loaders/page-context';
import {pageMetadata} from '@/shell/seo/metadata';

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
export default async function BlogPostPage({params, searchParams}: Props) {
    const [{slug}, {preview}] = await Promise.all([params, searchParams]);
    const [theme, ctx, data] = await Promise.all([getTheme(), loadPageContext(), loadBlogPost(slug, preview)]);
    return <theme.pages.BlogPost ctx={ctx} data={data}/>;
}
