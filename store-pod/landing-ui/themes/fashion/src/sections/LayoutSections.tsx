import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import {linkHref, slidesAsBanners, type SectionRenderProps, type ThemeSectionRegistry} from '@store-front/theme';
import type {LayoutSectionData} from '@store-front/types';
import {cn} from '@store-front/ui/lib/utils';
import {HeadlineSheet, SlidePoster, type WallAction} from './Hero';
import {NewsletterSheet} from './NewsletterSheet';
import {ProductGrid} from '../components/ProductGrid';
import {PosterImage} from '../components/PosterImage';
import {SectionHeading} from '../components/SectionHeading';

/**
 * fashion's layout-section registry — the whole page is the wall. Every kind renders as something
 * pasted, printed or stamped on the plaster: sheets, day-glo paper, strips, rubber stamps. Nothing on
 * this page is allowed to read as a widget; the shell fallbacks never show through on fashion.
 */

const mediaUrl = (props: Record<string, unknown>): string | undefined =>
    typeof props.mediaUrl === 'string' ? props.mediaUrl : undefined;

/**
 * A slide's own CTA + link, as a pasted strip on the name sheet. A label the day-glo CTA already
 * carries — or a repeat among the slides — is not pasted twice: two identical SHOP NOW papers on one
 * sheet read as a print error, not emphasis.
 */
function stripActions(section: LayoutSectionData): WallAction[] {
    const taken = new Set([section.text.cta?.trim().toLowerCase() ?? '']);
    const strips: WallAction[] = [];
    for (const item of section.items ?? []) {
        const label = item.text.cta?.trim() ?? '';
        const href = linkHref(item.props.link);
        if (!label || href === '#' || taken.has(label.toLowerCase())) continue;
        taken.add(label.toLowerCase());
        strips.push({label, href});
    }
    return strips;
}

function HeroWall({ctx, section}: SectionRenderProps) {
    const slides = slidesAsBanners(section.items);
    const [first, ...rest] = slides;
    const storeName = ctx.store.name;
    const ctaHref = linkHref(section.props.link);
    const cta = section.text.cta && ctaHref !== '#' ? {label: section.text.cta, href: ctaHref} : undefined;
    const firstLink = linkHref(section.items?.[0]?.props.link);
    const firstPoster = first && (
        <SlidePoster slide={first} index={0} total={slides.length} storeName={storeName} priority
                     ratio="16 / 10" className="h-full lg:aspect-auto [--tilt:0.8deg]"/>
    );
    return (
        // A wall needs margin around its sheets: even at the layout's full width the posters keep
        // gutters instead of touching the viewport edge, or the paste-up reads as a print error.
        <div className="mx-auto grid w-full max-w-(--container-wide) grid-cols-2 gap-3 px-4 sm:gap-4 sm:px-6 lg:grid-cols-12 lg:items-stretch">
            <HeadlineSheet ctx={ctx} heading={section.text.heading} subheading={section.text.subheading}
                           cta={cta} strips={stripActions(section)}
                           className={first ? 'col-span-2 lg:col-span-5 [--tilt:-0.7deg]' : 'col-span-2 lg:col-span-12 [--tilt:-0.7deg]'}/>
            {first && (
                <div className="col-span-2 min-w-0 lg:col-span-7">
                    {firstLink === '#' ? firstPoster : <Link prefetch={false} href={firstLink} className="block h-full">{firstPoster}</Link>}
                </div>
            )}
            {rest.map((slide, index) => (
                <SlidePoster key={slide.id} slide={slide} index={index + 1} total={slides.length} storeName={storeName}
                             ratio="4 / 3" className={cn('col-span-1 lg:col-span-4', index % 2 === 0 ? '[--tilt:0.6deg]' : '[--tilt:-0.8deg]')}/>
            ))}
        </div>
    );
}

function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const products = data?.products?.products ?? [];
    if (products.length === 0) return null;
    const title = section.text.title ?? data?.products?.title;
    const leadSrc = section.variant === 'rail' ? mediaUrl(section.props) : undefined;
    const lead = leadSrc && (
        <figure className="sheet sheen peel relative h-full min-h-64 min-w-0 overflow-hidden">
            <PosterImage src={leadSrc} alt={title ?? ''} title={ctx.store.name} tone="faint"
                         sizes="(max-width: 1024px) 100vw, 40vw"/>
        </figure>
    );
    // The interleave: a `rail` section with a lead image pastes it as the wide poster inside the grid,
    // the old wall's signature; without one, the stretch is a plain run of product sheets.
    const grid = lead ? {base: 2 as const, sm: 3, lg: 4, xl: 6} : ctx.layout.productGrid;
    return (
        <section className="min-w-0">
            {title && <SectionHeading title={title} meta={products.length}/>}
            <ProductGrid products={products} storeContext={ctx.storeContext} grid={grid} lead={lead || undefined}/>
        </section>
    );
}

/** Trust badges as rubber stamps: overprints on the wall, never tinted cards. */
function UspStamps({section}: SectionRenderProps) {
    const badges = (section.items ?? []).filter(badge => badge.text.title);
    if (badges.length === 0) return null;
    return (
        <section className="min-w-0">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <ul className="flex flex-wrap items-start justify-center gap-x-10 gap-y-6">
                {badges.map(badge => (
                    <li key={badge.id} className="flex max-w-48 flex-col items-center gap-2 text-center">
                        <span className="stamp"><bdi dir="auto">{badge.text.title}</bdi></span>
                        {badge.text.body && (
                            <span className="text-xs uppercase tracking-wide text-muted-foreground">
                                <bdi dir="auto">{badge.text.body}</bdi>
                            </span>
                        )}
                    </li>
                ))}
            </ul>
        </section>
    );
}

/** Categories as pasted strips — the destinations, slapped on the wall the way the nav wears them. */
function CategoryStrips({section, data}: SectionRenderProps) {
    const categories = data?.categories ?? [];
    if (categories.length === 0) return null;
    return (
        <section className="min-w-0">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <div className="wall flex flex-wrap gap-3">
                {categories.map(category => (
                    <Link key={category.id} prefetch={false} href={`/category/${category.description?.friendlyUrl ?? category.code}`}
                          className="strip strip-hover h-11 text-sm">
                        <bdi dir="auto">{category.description?.name ?? category.code}</bdi>
                        {typeof category.productCount === 'number' && category.productCount > 0 && (
                            <span className="tabular-nums text-muted-foreground">{category.productCount}</span>
                        )}
                    </Link>
                ))}
            </div>
        </section>
    );
}

/** The promo as a run of day-glo tape across the wall. */
function PromoTape({section}: SectionRenderProps) {
    const message = section.text.message;
    if (!message) return null;
    const href = linkHref(section.props.link);
    return (
        // The tape runs straight across the wall, like the announcement it echoes — no tilt.
        <div className="glo tape flex min-h-14 flex-wrap items-center justify-center gap-x-6 gap-y-2 px-5 py-3 text-center text-base">
            <bdi dir="auto">{message}</bdi>
            {section.text.cta && href !== '#' && (
                <Link prefetch={false} href={href} className="underline underline-offset-4">
                    <bdi dir="auto">{section.text.cta}</bdi>
                </Link>
            )}
        </div>
    );
}

function ImageSheet({ctx, section}: SectionRenderProps) {
    const src = mediaUrl(section.props);
    if (!src) return null;
    const contained = section.variant === 'contained';
    return (
        <figure className="sheet sheen peel relative overflow-hidden [--tilt:0.5deg]"
                style={{aspectRatio: contained ? '4 / 3' : '21 / 9'}}>
            <PosterImage src={src} alt={section.text.title ?? ''} title={ctx.store.name} tone="faint"
                         fit={contained ? 'contain' : 'cover'} sizes="(max-width: 1024px) 100vw, 1152px"/>
        </figure>
    );
}

/** Long copy as a printed sheet pinned to the wall. */
function RichTextSheet({section}: SectionRenderProps) {
    if (!section.text.body && !section.text.title) return null;
    return (
        <div className={cn('sheet sheen peel max-w-prose p-6 sm:p-8 [--tilt:-0.4deg]',
            section.variant === 'centered' && 'mx-auto text-center')}>
            {section.text.title && (
                <h2 className="mb-3 font-display text-2xl uppercase leading-tight"><bdi dir="auto">{section.text.title}</bdi></h2>
            )}
            {section.text.body && (
                // CMS-authored HTML, sanitized by the content service on write.
                <div className="text-sm leading-relaxed [&_a]:underline [&_a]:underline-offset-4 [&_p+p]:mt-3"
                     dangerouslySetInnerHTML={{__html: section.text.body}}/>
            )}
        </div>
    );
}

function FaqSheet({section, data}: SectionRenderProps) {
    const limit = typeof section.props.limit === 'number' ? section.props.limit : 5;
    const entries = (data?.faq?.groups ?? []).flatMap(group => group.entries).slice(0, limit);
    if (entries.length === 0) return null;
    return (
        <section className="mx-auto min-w-0 max-w-2xl">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <div className="sheet sheen peel divide-y px-5 py-2 sm:px-7 [--tilt:0.3deg]">
                {entries.map((entry, index) => (
                    <details key={index} className="group py-3">
                        <summary className="flex cursor-pointer list-none items-baseline justify-between gap-3 font-display text-sm uppercase tracking-wide marker:hidden [&::-webkit-details-marker]:hidden">
                            <bdi dir="auto">{entry.question}</bdi>
                            <span aria-hidden className="text-muted-foreground transition-transform group-open:rotate-45">+</span>
                        </summary>
                        <div className="pt-2 text-sm leading-relaxed text-muted-foreground"
                             dangerouslySetInnerHTML={{__html: entry.answer}}/>
                    </details>
                ))}
            </div>
        </section>
    );
}

/** Quotes as smaller sheets pasted around the stretch. */
function TestimonialSheets({section}: SectionRenderProps) {
    const quotes = (section.items ?? []).filter(quote => quote.text.quote);
    if (quotes.length === 0) return null;
    return (
        <section className="min-w-0">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <div className="wall grid gap-4 md:grid-cols-3">
                {quotes.map(quote => (
                    <figure key={quote.id} className="sheet sheen flex flex-col p-5">
                        {/* the quote is set in the poster voice — a caption pasted on the wall, not body copy */}
                        <blockquote className="font-display text-xl leading-snug"><bdi dir="auto">“{quote.text.quote}”</bdi></blockquote>
                        {quote.text.author && (
                            <figcaption className="mt-auto pt-3 text-xs uppercase tracking-[0.15em] text-muted-foreground">
                                <bdi dir="auto">{quote.text.author}</bdi>
                            </figcaption>
                        )}
                    </figure>
                ))}
            </div>
        </section>
    );
}

/** Brand marks as small pasted labels: the logo on paper with the name printed under it, so a label
 * still reads even before (or without) its artwork. */
function BrandLabels({section}: SectionRenderProps) {
    const logos = (section.items ?? []).filter(logo => mediaUrl(logo.props) || logo.text.name);
    if (logos.length === 0) return null;
    return (
        <section className="min-w-0">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <div className="wall flex flex-wrap items-center justify-center gap-4">
                {logos.map(logo => {
                    const href = linkHref(logo.props.link);
                    const src = mediaUrl(logo.props);
                    const name = logo.text.name;
                    const label = (
                        <span className="sheet sheet-flat flex w-32 flex-col items-center gap-1.5 p-3">
                            {src && (
                                <span className="relative block h-10 w-full">
                                    <Image src={src} alt={name ?? ''} fill className="object-contain"/>
                                </span>
                            )}
                            {name && (
                                <span className="font-display text-[0.65rem] uppercase tracking-[0.18em]">
                                    <bdi dir="auto">{name}</bdi>
                                </span>
                            )}
                        </span>
                    );
                    return href === '#'
                        ? <span key={logo.id}>{label}</span>
                        : <Link key={logo.id} prefetch={false} href={href} className="sheet-lift block">{label}</Link>;
                })}
            </div>
        </section>
    );
}

/** YouTube/Vimeo page URL → privacy-friendly embed URL; undefined for anything else. */
function embedUrl(raw: unknown): string | undefined {
    if (typeof raw !== 'string' || !raw) return undefined;
    try {
        const url = new URL(raw);
        const host = url.hostname.replace(/^www\./, '');
        if (host === 'youtube.com' && url.searchParams.get('v')) {
            return `https://www.youtube-nocookie.com/embed/${url.searchParams.get('v')}`;
        }
        if (host === 'youtu.be' && url.pathname.length > 1) {
            return `https://www.youtube-nocookie.com/embed/${url.pathname.slice(1)}`;
        }
        if (host === 'vimeo.com' && /^\/\d+/.test(url.pathname)) {
            return `https://player.vimeo.com/video/${url.pathname.slice(1)}`;
        }
    } catch {
        return undefined;
    }
    return undefined;
}

/** The video pasted like any other sheet: a paper frame around the picture. */
function VideoSheet({section}: SectionRenderProps) {
    const src = embedUrl(section.props.url);
    if (!src) return null;
    return (
        <section className="mx-auto min-w-0 max-w-3xl">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <div className="sheet sheen p-2 pb-1 [--tilt:0.4deg]">
                {/* the striped typo-poster paper sits behind the player, so a slow or blocked embed
                    still reads as a sheet on the wall rather than a blank hole */}
                <div className="typo-poster relative aspect-video w-full overflow-hidden">
                    <iframe src={src} title={section.text.title ?? 'Video'} className="absolute inset-0 size-full"
                            allow="accelerometer; encrypted-media; picture-in-picture" allowFullScreen
                            loading="lazy" referrerPolicy="no-referrer"/>
                </div>
                <p className="px-1 py-1.5 text-[0.65rem] uppercase tracking-[0.2em] text-muted-foreground">
                    <bdi dir="auto">{section.text.title ?? 'Video'}</bdi>
                </p>
            </div>
        </section>
    );
}

/** Posts as magazine covers pasted in a row. */
function PostCovers({section, data}: SectionRenderProps) {
    const posts = data?.posts?.content ?? [];
    if (posts.length === 0) return null;
    return (
        <section className="min-w-0">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <div className="wall grid gap-4 sm:grid-cols-2 md:grid-cols-3">
                {posts.map(post => (
                    <Link key={post.id} prefetch={false} href={`/blog/${post.slug}`} className="sheet sheen peel sheet-lift group block overflow-hidden">
                        {post.heroImageUrl && (
                            <span className="relative block aspect-[3/2] overflow-hidden bg-muted">
                                <Image src={post.heroImageUrl} alt={post.title} fill className="object-cover"/>
                            </span>
                        )}
                        <span className="block p-4">
                            <span className="font-display text-lg uppercase leading-tight group-hover:underline group-hover:underline-offset-4">
                                <bdi dir="auto">{post.title}</bdi>
                            </span>
                            {post.excerpt && (
                                <span className="mt-1 line-clamp-2 block text-sm text-muted-foreground">
                                    <bdi dir="auto">{post.excerpt}</bdi>
                                </span>
                            )}
                        </span>
                    </Link>
                ))}
            </div>
        </section>
    );
}

export const layoutSections: ThemeSectionRegistry = {
    hero: {classic: HeroWall, carousel: HeroWall, split: HeroWall},
    products: {grid: ProductsSection, rail: ProductsSection},
    usp: {row: UspStamps},
    categories: {grid: CategoryStrips, pills: CategoryStrips},
    promo: {strip: PromoTape, card: PromoTape},
    image: {full: ImageSheet, contained: ImageSheet},
    richtext: {default: RichTextSheet, centered: RichTextSheet},
    faq: {accordion: FaqSheet},
    testimonials: {cards: TestimonialSheets, quotes: TestimonialSheets},
    brands: {row: BrandLabels},
    video: {embed: VideoSheet},
    posts: {cards: PostCovers},
    newsletter: {inline: NewsletterSheet, boxed: NewsletterSheet},
};
