import {getTranslations} from 'next-intl/server';
import {Link} from '@store-front/i18n/navigation';
import type {BlogIndexData, PageProps} from '@store-front/theme';
import type {PostSummary} from '@store-front/types';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {PageHead} from '../components/PageHead';

function PostCard({post, t}: { post: PostSummary; t: (k: string, v?: Record<string, string | number>) => string }) {
    return (
        <article className="flex h-full flex-col gap-3.5">
            {post.heroImageUrl && (
                <Link prefetch={false} href={`/blog/${post.slug}`} className="window block aspect-[16/10]">
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img src={post.heroImageUrl} alt="" className="size-full object-cover"/>
                </Link>
            )}
            {post.categories.length > 0 && (
                <p className="sign text-[0.625rem] text-muted-foreground">{post.categories.map(c => c.title).join(' · ')}</p>
            )}
            <h2 className="text-lg leading-snug">
                <Link prefetch={false} href={`/blog/${post.slug}`} className="hover:underline">{post.title}</Link>
            </h2>
            {post.excerpt && <p className="line-clamp-3 text-sm text-muted-foreground">{post.excerpt}</p>}
            <p className="rule-brass mt-auto border-t pt-3 text-xs text-muted-foreground">
                {post.publishedAt && <time className="figure" dateTime={post.publishedAt}>{new Date(post.publishedAt).toLocaleDateString()}</time>}
                {post.readingMinutes > 0 && <> · {t('MIN_READ', {minutes: post.readingMinutes})}</>}
            </p>
        </article>
    );
}

export async function BlogIndex({data}: PageProps<BlogIndexData>) {
    const t = await getTranslations('PAGE.BLOG');
    const {posts} = data;
    const filter = data.category ?? data.tag;
    const pageHref = (p: number) => `/blog?page=${p}${data.category ? `&category=${data.category}` : ''}${data.tag ? `&tag=${data.tag}` : ''}`;
    const chip = 'sign rule-brass rounded-control border px-3.5 py-2 text-[0.625rem] transition-colors duration-(--motion-fast)';
    return (
        <PageShell className="flex flex-col gap-8 py-6 lg:gap-12 lg:py-10">
            <Breadcrumbs items={data.breadcrumbs}/>
            <PageHead title={t('TITLE')} meta={filter ? t('FILTERED_BY', {value: filter}) : t('SUBTITLE')}/>
            {data.categories.length > 0 && (
                <nav className="flex flex-wrap gap-2.5" aria-label={t('TITLE')}>
                    <Link prefetch={false} href="/blog" className={`${chip} ${!data.category ? 'border-transparent bg-primary text-primary-foreground' : 'hover:bg-secondary'}`}>{t('ALL_POSTS')}</Link>
                    {data.categories.map(c => (
                        <Link key={c.slug} prefetch={false} href={c.href} className={`${chip} ${data.category === c.slug ? 'border-transparent bg-primary text-primary-foreground' : 'hover:bg-secondary'}`}>{c.title}</Link>
                    ))}
                </nav>
            )}
            {posts.content.length === 0 ? (
                <p className="py-12 text-center text-muted-foreground">{t('EMPTY')}</p>
            ) : (
                <ul className="grid gap-x-8 gap-y-12 sm:grid-cols-2 lg:grid-cols-3">
                    {posts.content.map(post => <li key={post.id} className="flex"><PostCard post={post} t={t}/></li>)}
                </ul>
            )}
            {posts.totalPages > 1 && (
                <nav className="rule-brass flex items-center justify-between border-t pt-6" aria-label="pagination">
                    {posts.pageNumber > 0 ? <Link prefetch={false} href={pageHref(posts.pageNumber - 1)} className="sign text-[0.625rem] underline">{t('PREVIOUS')}</Link> : <span/>}
                    <span className="figure text-sm text-muted-foreground">{posts.pageNumber + 1} / {posts.totalPages}</span>
                    {posts.pageNumber + 1 < posts.totalPages ? <Link prefetch={false} href={pageHref(posts.pageNumber + 1)} className="sign text-[0.625rem] underline">{t('NEXT')}</Link> : <span/>}
                </nav>
            )}
        </PageShell>
    );
}
