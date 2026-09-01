import {Link} from '@store-front/i18n/navigation';
import {linkHref, slidesAsBanners, type SectionRenderProps, type ThemeSectionRegistry} from '@store-front/theme';
import {Masthead} from './Masthead';
import {NewsletterSection} from './NewsletterSection';
import {ProductGrid} from '../components/ProductGrid';
import {SectionHeading} from '../components/SectionHeading';

/**
 * hunger's layout-section registry — the sheet. The hero is the theme's Masthead; product sections are
 * printed as menu sections. A menu does not hide its dishes behind arrows, so the `rail` variant sets
 * the dense dish-line list instead of a carousel, and `grid` sets the larger board face.
 */

function HeroSection({ctx, section}: SectionRenderProps) {
    // The masthead is print, not a flood: at the layout's default full width it still keeps the
    // sheet's own margins instead of running its caps into the viewport edge.
    return (
        <div className="mx-auto w-full max-w-(--container-wide) px-4 sm:px-6">
            <Masthead store={ctx.store} slides={slidesAsBanners(section.items)}/>
        </div>
    );
}

function ProductsSection({ctx, section, data}: SectionRenderProps) {
    const products = data?.products?.products ?? [];
    if (products.length === 0) return null;
    const title = section.text.title ?? data?.products?.title;
    return (
        <section className="min-w-0">
            {title && <SectionHeading title={title} subtitle={section.text.subtitle}/>}
            <ProductGrid products={products} storeContext={ctx.storeContext} grid={ctx.layout.productGrid}
                         variant={section.variant === 'rail' ? 'line' : 'board'}/>
        </section>
    );
}

/** Trust badges as printed marks — outline overprints, ink only, never a tinted card. */
function UspMarks({section}: SectionRenderProps) {
    const badges = (section.items ?? []).filter(badge => badge.text.title);
    if (badges.length === 0) return null;
    return (
        <section className="min-w-0">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <ul className="flex flex-wrap items-start justify-center gap-x-8 gap-y-4">
                {badges.map(badge => (
                    <li key={badge.id} className="flex max-w-52 flex-col items-center gap-1.5 text-center">
                        <span className="mark"><bdi dir="auto">{badge.text.title}</bdi></span>
                        {badge.text.body && (
                            <span className="text-xs text-muted-foreground"><bdi dir="auto">{badge.text.body}</bdi></span>
                        )}
                    </li>
                ))}
            </ul>
        </section>
    );
}

/** Categories as the menu's own section bands, each a line to a stretch of the sheet. */
function CategoryBands({section, data}: SectionRenderProps) {
    const categories = data?.categories ?? [];
    if (categories.length === 0) return null;
    return (
        <section className="mx-auto min-w-0 max-w-2xl">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <ul className="flex flex-col gap-2">
                {categories.map(category => (
                    <li key={category.id}>
                        <Link prefetch={false} href={`/category/${category.description?.friendlyUrl ?? category.code}`}
                              className="plate flex items-baseline justify-between gap-4 px-3 py-2 transition-opacity hover:opacity-90">
                            <span className="press text-lg"><bdi dir="auto">{category.description?.name ?? category.code}</bdi></span>
                            {typeof category.productCount === 'number' && category.productCount > 0 && (
                                <span className="price text-sm">{category.productCount}</span>
                            )}
                        </Link>
                    </li>
                ))}
            </ul>
        </section>
    );
}

/** The promo printed as a band of the primary — the second plate, nothing tinted. */
function PromoBand({section}: SectionRenderProps) {
    const message = section.text.message;
    if (!message) return null;
    const href = linkHref(section.props.link);
    return (
        <div className="plate flex min-h-12 flex-wrap items-center justify-center gap-x-6 gap-y-2 px-5 py-3 text-center">
            <span className="press text-lg"><bdi dir="auto">{message}</bdi></span>
            {section.text.cta && href !== '#' && (
                <Link prefetch={false} href={href} className="text-sm font-bold underline underline-offset-4">
                    <bdi dir="auto">{section.text.cta}</bdi>
                </Link>
            )}
        </div>
    );
}

/** Questions as the menu's printed notes: ruled lines, no boxes. */
function FaqNotes({section, data}: SectionRenderProps) {
    const limit = typeof section.props.limit === 'number' ? section.props.limit : 5;
    const entries = (data?.faq?.groups ?? []).flatMap(group => group.entries).slice(0, limit);
    if (entries.length === 0) return null;
    return (
        <section className="mx-auto min-w-0 max-w-2xl">
            {section.text.title && <SectionHeading title={section.text.title}/>}
            <div className="divide-y divide-foreground/25 border-y border-foreground/25">
                {entries.map((entry, index) => (
                    <details key={index} className="group py-3">
                        <summary className="press flex cursor-pointer list-none items-baseline justify-between gap-3 text-base marker:hidden [&::-webkit-details-marker]:hidden">
                            <bdi dir="auto">{entry.question}</bdi>
                            <span aria-hidden className="text-muted-foreground transition-transform group-open:rotate-45">+</span>
                        </summary>
                        <div className="prose-hunger pt-2 text-sm text-muted-foreground"
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
                <div className="prose-hunger text-sm [&_a]:underline [&_a]:underline-offset-4"
                     dangerouslySetInnerHTML={{__html: section.text.body}}/>
            )}
        </div>
    );
}

export const layoutSections: ThemeSectionRegistry = {
    hero: {classic: HeroSection, carousel: HeroSection},
    products: {rail: ProductsSection, grid: ProductsSection},
    usp: {row: UspMarks},
    categories: {grid: CategoryBands, pills: CategoryBands},
    promo: {strip: PromoBand, card: PromoBand},
    faq: {accordion: FaqNotes},
    newsletter: {inline: NewsletterSection, boxed: NewsletterSection},
    richtext: {default: RichTextSection, centered: RichTextSection},
};
