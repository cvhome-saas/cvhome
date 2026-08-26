import {getTranslations} from 'next-intl/server';
import {Link} from '@store-front/i18n/navigation';
import type {BlogPostData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {SectionHeading} from '../components/SectionHeading';

export async function BlogPost({data}: PageProps<BlogPostData>) {
    const t = await getTranslations('PAGE.BLOG');
    const {post} = data;
    return (
        <PageShell width="narrow" className="flex flex-col gap-8 py-6 lg:gap-12 lg:py-10">
            <Breadcrumbs items={data.breadcrumbs}/>
            <article className="flex flex-col gap-6">
                <header className="flex flex-col gap-4">
                    {post.categories.length > 0 && (
                        <p className="sign text-[0.625rem] text-muted-foreground">
                            {post.categories.map((c, i) => <span key={c.slug}>{i > 0 && ' · '}<Link prefetch={false} href={c.href} className="hover:underline">{c.title}</Link></span>)}
                        </p>
                    )}
                    <h1 className="sign-lg text-2xl lg:text-4xl">{post.title}</h1>
                    <p className="rule-brass border-b pb-4 text-sm text-muted-foreground">
                        {post.authorName && <>{t('BY', {author: post.authorName})} · </>}
                        {post.publishedAt && <time className="figure" dateTime={post.publishedAt}>{new Date(post.publishedAt).toLocaleDateString()}</time>}
                        {post.readingMinutes > 0 && <> · {t('MIN_READ', {minutes: post.readingMinutes})}</>}
                    </p>
                </header>
                {post.heroImageUrl && (
                    <div className="window aspect-[16/9]">
                        {/* eslint-disable-next-line @next/next/no-img-element */}
                        <img src={post.heroImageUrl} alt="" className="size-full object-cover"/>
                    </div>
                )}
                <div className="copy" dangerouslySetInnerHTML={{__html: data.html}}/>
                {post.tags.length > 0 && (
                    <ul className="flex flex-wrap gap-2">
                        {post.tags.map(tag => (
                            <li key={tag}>
                                <Link prefetch={false} href={`/blog?tag=${encodeURIComponent(tag)}`}
                                      className="sign rule-brass rounded-control border px-2.5 py-1.5 text-[0.625rem] hover:bg-secondary">#{tag}</Link>
                            </li>
                        ))}
                    </ul>
                )}
            </article>
            {post.related && post.related.length > 0 && (
                <aside>
                    <SectionHeading as="h2" title={t('RELATED')}/>
                    <ul className="grid gap-6 sm:grid-cols-3">
                        {post.related.map(r => (
                            <li key={r.id} className="rule-brass border-t pt-3">
                                <Link prefetch={false} href={`/blog/${r.slug}`} className="text-sm hover:underline">{r.title}</Link>
                                {r.excerpt && <p className="mt-1.5 line-clamp-2 text-sm text-muted-foreground">{r.excerpt}</p>}
                            </li>
                        ))}
                    </ul>
                </aside>
            )}
        </PageShell>
    );
}
