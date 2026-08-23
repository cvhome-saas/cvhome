import {getTranslations} from 'next-intl/server';
import {Link} from '@store-front/i18n/navigation';
import type {BlogPostData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';

const PROSE = 'leading-relaxed [&_a]:underline [&_h2]:mb-3 [&_h2]:mt-8 [&_h2]:text-xl [&_h2]:font-semibold [&_h3]:mb-2 [&_h3]:mt-6 [&_h3]:text-lg [&_h3]:font-semibold [&_img]:my-6 [&_img]:rounded-card [&_li]:mb-1 [&_ol]:list-decimal [&_ol]:ps-5 [&_p]:mb-4 [&_ul]:list-disc [&_ul]:ps-5';

export async function BlogPost({data}: PageProps<BlogPostData>) {
    const t = await getTranslations('PAGE.BLOG');
    const {post} = data;
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <article className="flex flex-col gap-6">
                <header className="flex flex-col gap-3">
                    {post.categories.length > 0 && (
                        <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                            {post.categories.map((c, i) => <span key={c.slug}>{i > 0 && ' · '}<Link prefetch={false} href={c.href} className="hover:underline">{c.title}</Link></span>)}
                        </p>
                    )}
                    <h1 className="text-3xl font-semibold tracking-tight">{post.title}</h1>
                    <p className="text-sm text-muted-foreground">
                        {post.authorName && <>{t('BY', {author: post.authorName})} · </>}
                        {post.publishedAt && <time dateTime={post.publishedAt}>{new Date(post.publishedAt).toLocaleDateString()}</time>}
                        {post.readingMinutes > 0 && <> · {t('MIN_READ', {minutes: post.readingMinutes})}</>}
                    </p>
                </header>
                {post.heroImageUrl && (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={post.heroImageUrl} alt="" className="aspect-[16/9] w-full rounded-card object-cover"/>
                )}
                <div className={PROSE} dangerouslySetInnerHTML={{__html: data.html}}/>
                {post.tags.length > 0 && (
                    <ul className="flex flex-wrap gap-2">
                        {post.tags.map(tag => <li key={tag}><Link prefetch={false} href={`/blog?tag=${encodeURIComponent(tag)}`} className="rounded-control border px-2 py-0.5 text-xs hover:bg-muted">#{tag}</Link></li>)}
                    </ul>
                )}
            </article>
            {post.related && post.related.length > 0 && (
                <aside className="border-t pt-6">
                    <h2 className="mb-3 text-lg font-semibold">{t('RELATED')}</h2>
                    <ul className="grid gap-3 sm:grid-cols-3">
                        {post.related.map(r => (
                            <li key={r.id} className="rounded-card border p-3">
                                <Link prefetch={false} href={`/blog/${r.slug}`} className="font-medium hover:underline">{r.title}</Link>
                                {r.excerpt && <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">{r.excerpt}</p>}
                            </li>
                        ))}
                    </ul>
                </aside>
            )}
        </PageShell>
    );
}
