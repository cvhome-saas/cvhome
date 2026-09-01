import {Link} from '@store-front/i18n/navigation';
import {linkHref, slidesAsBanners, type SectionRenderProps, type ThemeSectionRegistry} from '@store-front/theme';
import {NewsletterSection} from './NewsletterSection';
import {Hero} from './Hero';
import {ProductGrid} from '../components/ProductGrid';
import {ProductRail} from './ProductRail';
import {SectionHeading} from '../components/SectionHeading';

/**
 * basic's layout-section registry: the theme's own hero and product surfaces, wired to the builder's
 * document. Everything unregistered renders through the shell fallbacks.
 */

function HeroSection({ctx, section}: SectionRenderProps) {
    const slides = slidesAsBanners(section.items);
    if (slides.length === 0 && !section.text.heading) return null;
    return <Hero slides={slides} banner={slides[0]} storeName={section.text.heading ?? ctx.store.name}
                 facts={[]} anchor={section.anchor ?? undefined}/>;
}

function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const products = data?.products?.products ?? [];
    if (products.length === 0) return null;
    const title = section.text.title ?? data?.products?.title;
    const rail = section.variant === 'rail';
    return (
        <section>
            {title && <SectionHeading title={title} subtitle={section.text.subtitle} className="mb-4"/>}
            {rail
                ? <ProductRail products={products} storeContext={ctx.storeContext}/>
                : <ProductGrid products={products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}/>}
        </section>
    );
}


/** Trust badges as chips — the theme's own quiet mark, never a grey icon grid. */
function UspChips({section}: SectionRenderProps) {
    const badges = (section.items ?? []).filter(badge => badge.text.title);
    if (badges.length === 0) return null;
    return (
        <section className="min-w-0">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <ul className="flex flex-wrap items-start justify-center gap-x-6 gap-y-3">
                {badges.map(badge => (
                    <li key={badge.id} className="flex max-w-52 flex-col items-center gap-1.5 text-center">
                        <span className="chip"><bdi dir="auto">{badge.text.title}</bdi></span>
                        {badge.text.body && (
                            <span className="text-xs text-muted-foreground"><bdi dir="auto">{badge.text.body}</bdi></span>
                        )}
                    </li>
                ))}
            </ul>
        </section>
    );
}

/** Categories as the catalogue's index tabs. */
function CategoryTabs({section, data}: SectionRenderProps) {
    const categories = data?.categories ?? [];
    if (categories.length === 0) return null;
    return (
        <section className="min-w-0">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <div className="flex flex-wrap gap-2">
                {categories.map(category => (
                    <Link key={category.id} prefetch={false} href={`/category/${category.description?.friendlyUrl ?? category.code}`}
                          className="index-tab">
                        <bdi dir="auto">{category.description?.name ?? category.code}</bdi>
                        {typeof category.productCount === 'number' && category.productCount > 0 && (
                            <span className="text-xs tabular-nums text-muted-foreground">{category.productCount}</span>
                        )}
                    </Link>
                ))}
            </div>
        </section>
    );
}

/** The promo as one plain primary band. */
function PromoBand({section}: SectionRenderProps) {
    const message = section.text.message;
    if (!message) return null;
    const href = linkHref(section.props.link);
    return (
        <div className="flex min-h-12 flex-wrap items-center justify-center gap-x-6 gap-y-2 bg-primary px-5 py-3 text-center text-primary-foreground">
            <span className="text-sm font-semibold uppercase tracking-wide"><bdi dir="auto">{message}</bdi></span>
            {section.text.cta && href !== '#' && (
                <Link prefetch={false} href={href} className="text-sm underline underline-offset-4">
                    <bdi dir="auto">{section.text.cta}</bdi>
                </Link>
            )}
        </div>
    );
}

function FaqList({section, data}: SectionRenderProps) {
    const limit = typeof section.props.limit === 'number' ? section.props.limit : 5;
    const entries = (data?.faq?.groups ?? []).flatMap(group => group.entries).slice(0, limit);
    if (entries.length === 0) return null;
    return (
        <section className="mx-auto min-w-0 max-w-2xl">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <div className="divide-y border-y">
                {entries.map((entry, index) => (
                    <details key={index} className="group py-3">
                        <summary className="flex cursor-pointer list-none items-baseline justify-between gap-3 font-display text-sm font-semibold uppercase tracking-wide marker:hidden [&::-webkit-details-marker]:hidden">
                            <bdi dir="auto">{entry.question}</bdi>
                            <span aria-hidden className="text-muted-foreground transition-transform group-open:rotate-45">+</span>
                        </summary>
                        <div className="prose-basic pt-2 text-sm text-muted-foreground"
                             dangerouslySetInnerHTML={{__html: entry.answer}}/>
                    </details>
                ))}
            </div>
        </section>
    );
}

function RichTextSection({section}: SectionRenderProps) {
    if (!section.text.body && !section.text.title) return null;
    return (
        <div className={section.variant === 'centered' ? 'mx-auto max-w-prose text-center' : 'max-w-prose'}>
            {section.text.title && <SectionHeading title={section.text.title}/>}
            {section.text.body && (
                // CMS-authored HTML, sanitized by the content service on write.
                <div className="prose-basic text-sm [&_a]:underline [&_a]:underline-offset-4"
                     dangerouslySetInnerHTML={{__html: section.text.body}}/>
            )}
        </div>
    );
}

export const layoutSections: ThemeSectionRegistry = {
    usp: {row: UspChips},
    categories: {grid: CategoryTabs, pills: CategoryTabs},
    promo: {strip: PromoBand, card: PromoBand},
    faq: {accordion: FaqList},
    newsletter: {inline: NewsletterSection, boxed: NewsletterSection},
    richtext: {default: RichTextSection, centered: RichTextSection},

    hero: {classic: HeroSection, carousel: HeroSection},
    products: {rail: ProductsSection, grid: ProductsSection},
};
