import {getTranslations} from 'next-intl/server';
import {Link} from '@store-front/i18n/navigation';
import type {BlogPostData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {PROSE} from '../components/prose';

export async function BlogPost({data}: PageProps<BlogPostData>) {
    const t = await getTranslations('PAGE.BLOG');
    const {post} = data;
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-8 lg:py-12">
            <Breadcrumbs items={data.breadcrumbs}/>
            <article className="flex flex-col gap-6">
                <header className="hair flex flex-col gap-3 border-b-2 pb-5">
                    {post.categories.length > 0 && (
                        <p className="cover-line text-muted-foreground">
                            {post.categories.map((c, i) => <span key={c.slug}>{i > 0 && ' · '}<Link prefetch={false} href={c.href} className="hover:underline">{c.title}</Link></span>)}
                        </p>
                    )}
                    <h1 className="display text-3xl sm:text-4xl">{post.title}</h1>
                    <p className="figure text-sm text-muted-foreground">
                        {post.authorName && <>{t('BY', {author: post.authorName})} · </>}
                        {post.publishedAt && <time dateTime={post.publishedAt}>{new Date(post.publishedAt).toLocaleDateString()}</time>}
                        {post.readingMinutes > 0 && <> · {t('MIN_READ', {minutes: post.readingMinutes})}</>}
                    </p>
                </header>
                {post.heroImageUrl && (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={post.heroImageUrl} alt="" className="hair aspect-[16/9] w-full border object-cover"/>
                )}
                <div className={PROSE} dangerouslySetInnerHTML={{__html: data.html}}/>
                {post.tags.length > 0 && (
                    <ul className="flex flex-wrap gap-2">
                        {post.tags.map(tag => (
                            <li key={tag}>
                                <Link prefetch={false} href={`/blog?tag=${encodeURIComponent(tag)}`} className="hair cover-line inline-flex border px-2.5 py-1 hover:bg-secondary">#{tag}</Link>
                            </li>
                        ))}
                    </ul>
                )}
            </article>
            {post.related && post.related.length > 0 && (
                <aside className="hair border-t-2 pt-6">
                    <h2 className="display mb-4 text-lg">{t('RELATED')}</h2>
                    <ul className="grid gap-3 sm:grid-cols-3">
                        {post.related.map(r => (
                            <li key={r.id} className="cell p-3">
                                <Link prefetch={false} href={`/blog/${r.slug}`} className="font-bold hover:underline">{r.title}</Link>
                                {r.excerpt && <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">{r.excerpt}</p>}
                            </li>
                        ))}
                    </ul>
                </aside>
            )}
        </PageShell>
    );
}
