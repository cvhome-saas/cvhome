import type {Metadata} from 'next';
import {loadBlogPost} from '@/shell/loaders/blog';
import {pageMetadata} from '@/shell/seo/metadata';
import {themed, type ThemeSource} from './theme-source';

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
export function blogPostPage(theme: ThemeSource) {
    return async function BlogPostPage({params, searchParams}: Props) {
        const [{slug}, {preview}] = await Promise.all([params, searchParams]);
        const [{theme: resolved, ctx}, data] = await Promise.all([themed(theme), loadBlogPost(slug, preview)]);
        return <resolved.pages.BlogPost ctx={ctx} data={data}/>;
    };
}
