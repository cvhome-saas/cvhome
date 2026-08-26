import {getTranslations} from 'next-intl/server';
import {Link} from '@store-front/i18n/navigation';
import type {BlogIndexData, PageProps} from '@store-front/theme';
import type {PostSummary} from '@store-front/types';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';

function PostCard({post, t}: { post: PostSummary; t: (k: string, v?: Record<string, string | number>) => string }) {
    return (
        <article className="cell flex h-full flex-col">
            {post.heroImageUrl && (
                <Link prefetch={false} href={`/blog/${post.slug}`} className="wash block aspect-[16/9] overflow-hidden">
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img src={post.heroImageUrl} alt="" className="size-full object-cover"/>
                </Link>
            )}
            <div className="hair flex flex-1 flex-col gap-2 border-t p-4">
                {post.categories.length > 0 && (
                    <p className="cover-line text-muted-foreground">{post.categories.map(c => c.title).join(' · ')}</p>
                )}
                <h2 className="display text-lg"><Link prefetch={false} href={`/blog/${post.slug}`} className="hover:underline">{post.title}</Link></h2>
                {post.excerpt && <p className="line-clamp-3 text-sm text-muted-foreground">{post.excerpt}</p>}
                <p className="figure mt-auto pt-2 text-xs text-muted-foreground">
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
        <>
            <header className="flood tone-light hair border-b-2">
                <PageShell className="flex flex-col gap-4 py-8 lg:py-12">
                    <Breadcrumbs items={data.breadcrumbs} className="crumbs-flood"/>
                    <h1 className="display text-4xl sm:text-5xl">{t('TITLE')}</h1>
                    <p className="cover-line">{filter ? t('FILTERED_BY', {value: filter}) : t('SUBTITLE')}</p>
                    {data.categories.length > 0 && (
                        <nav className="mt-1 flex flex-wrap gap-2" aria-label={t('TITLE')}>
                            <Link prefetch={false} href="/blog"
                                  className={`cover-line px-3 py-1.5 ${!data.category ? 'ink-field' : 'border border-current/50'}`}>{t('ALL_POSTS')}</Link>
                            {data.categories.map(c => (
                                <Link key={c.slug} prefetch={false} href={c.href}
                                      className={`cover-line px-3 py-1.5 ${data.category === c.slug ? 'ink-field' : 'border border-current/50'}`}>{c.title}</Link>
                            ))}
                        </nav>
                    )}
                </PageShell>
            </header>
            <PageShell className="flex flex-col gap-8 py-8">
                {posts.content.length === 0 ? (
                    <p className="py-12 text-center text-muted-foreground">{t('EMPTY')}</p>
                ) : (
                    <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
                        {posts.content.map(post => <PostCard key={post.id} post={post} t={t}/>)}
                    </div>
                )}
                {posts.totalPages > 1 && (
                    <nav className="hair flex items-center justify-between border-t-2 pt-4" aria-label="pagination">
                        {posts.pageNumber > 0 ? <Link prefetch={false} href={pageHref(posts.pageNumber - 1)} className="cover-line underline">{t('PREVIOUS')}</Link> : <span/>}
                        <span className="pagemark">{posts.pageNumber + 1} / {posts.totalPages}</span>
                        {posts.pageNumber + 1 < posts.totalPages ? <Link prefetch={false} href={pageHref(posts.pageNumber + 1)} className="cover-line underline">{t('NEXT')}</Link> : <span/>}
                    </nav>
                )}
            </PageShell>
        </>
    );
}
