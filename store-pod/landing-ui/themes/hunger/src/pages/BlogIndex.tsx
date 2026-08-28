import {getTranslations} from 'next-intl/server';
import {Link} from '@store-front/i18n/navigation';
import type {BlogIndexData, PageProps} from '@store-front/theme';
import type {PostSummary} from '@store-front/types';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';

function PostCard({post, t}: { post: PostSummary; t: (k: string, v?: Record<string, string | number>) => string }) {
    return (
        <article className="crop flex flex-col border border-foreground bg-card">
            {post.heroImageUrl && (
                <Link prefetch={false} href={`/blog/${post.slug}`} className="block aspect-[16/9] overflow-hidden border-b border-foreground bg-muted">
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img src={post.heroImageUrl} alt="" className="size-full object-cover"/>
                </Link>
            )}
            <div className="flex flex-1 flex-col gap-2 p-4">
                {post.categories.length > 0 && (
                    <p className="press text-xs tracking-wide text-muted-foreground">{post.categories.map(c => c.title).join(' · ')}</p>
                )}
                <h2 className="press text-xl leading-none"><Link prefetch={false} href={`/blog/${post.slug}`} className="hover:underline">{post.title}</Link></h2>
                {post.excerpt && <p className="line-clamp-3 text-sm text-muted-foreground">{post.excerpt}</p>}
                <p className="mt-auto pt-2 text-xs text-muted-foreground">
                    {post.publishedAt && <time dateTime={post.publishedAt}>{new Date(post.publishedAt).toLocaleDateString()}</time>}
                    {post.readingMinutes > 0 && <> · {t('MIN_READ', {minutes: post.readingMinutes})}</>}
                </p>
            </div>
        </article>
    );
}

export async function BlogIndex({data}: PageProps<BlogIndexData>) {
    const t = await getTranslations('PAGE.BLOG');
    const {posts} = data;
    const filter = data.category ?? data.tag;
    const pageHref = (p: number) => `/blog?page=${p}${data.category ? `&category=${data.category}` : ''}${data.tag ? `&tag=${data.tag}` : ''}`;
    return (
        <PageShell className="flex flex-col gap-8 py-6">
            <Breadcrumbs items={data.breadcrumbs}/>
            <header className="flex flex-col gap-2">
                <h1 className="press text-4xl leading-none">{t('TITLE')}</h1>
                <p className="text-sm text-muted-foreground">{filter ? t('FILTERED_BY', {value: filter}) : t('SUBTITLE')}</p>
                {data.categories.length > 0 && (
                    <nav className="mt-2 flex flex-wrap" aria-label={t('TITLE')}>
                        <Link prefetch={false} href="/blog" className="fold h-8 text-xs" data-active={!data.category ? 'true' : undefined}>{t('ALL_POSTS')}</Link>
                        {data.categories.map(c => (
                            <Link key={c.slug} prefetch={false} href={c.href} className="fold h-8 text-xs" data-active={data.category === c.slug ? 'true' : undefined}>{c.title}</Link>
                        ))}
                    </nav>
                )}
            </header>
            {posts.content.length === 0 ? (
                <p className="py-12 text-center text-muted-foreground">{t('EMPTY')}</p>
            ) : (
                <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                    {posts.content.map(post => <PostCard key={post.id} post={post} t={t}/>)}
                </div>
            )}
            {posts.totalPages > 1 && (
                <nav className="flex items-center justify-between" aria-label="pagination">
                    {posts.pageNumber > 0 ? <Link prefetch={false} href={pageHref(posts.pageNumber - 1)} className="underline">{t('PREVIOUS')}</Link> : <span/>}
                    <span className="text-sm text-muted-foreground">{posts.pageNumber + 1} / {posts.totalPages}</span>
                    {posts.pageNumber + 1 < posts.totalPages ? <Link prefetch={false} href={pageHref(posts.pageNumber + 1)} className="underline">{t('NEXT')}</Link> : <span/>}
                </nav>
            )}
        </PageShell>
    );
}
